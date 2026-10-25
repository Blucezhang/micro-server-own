package com.own.product.service;

import com.own.face.trade.TradeException;
import com.own.product.audit.domain.ProductPriceAudit;
import com.own.product.audit.repository.ProductPriceAuditRepository;
import com.own.product.controller.MerchantProductController;
import com.own.product.dao.ProductDao;
import com.own.product.dao.ProductPriceChangeEventDao;
import com.own.product.domain.Product;
import com.own.product.domain.ProductPriceChangeEvent;
import com.own.product.dto.MerchantProductCommand;
import jakarta.persistence.EntityManagerFactory;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = ProductPriceSingleSourceUpdateTest.TestConfig.class)
public class ProductPriceSingleSourceUpdateTest {

    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(
            basePackages = "com.own.product.audit.repository",
            transactionManagerRef = "transactionManager")
    static class TestConfig {

        @Bean
        public DataSource dataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:product_price_audit_test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
            ds.setUsername("sa");
            ds.setPassword("");
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new FileSystemResource(resolveSqlPath("27-product-price-audit.sql")));
            populator.addScript(new FileSystemResource(resolveSqlPath("40-product-price-audit-event.sql")));
            populator.execute(ds);
            return ds;
        }

        private static String resolveSqlPath(String fileName) {
            if (Paths.get("database", "mysql", fileName).toFile().exists()) {
                return Paths.get("database", "mysql", fileName).toString();
            }
            return Paths.get("..", "database", "mysql", fileName).normalize().toString();
        }

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(dataSource);
            factory.setPackagesToScan("com.own.product.audit.domain");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            Properties jpaProps = new Properties();
            jpaProps.setProperty("hibernate.hbm2ddl.auto", "validate");
            jpaProps.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            factory.setJpaProperties(jpaProps);
            return factory;
        }

        @Bean(name = "transactionManager")
        @Primary
        public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
            return new JpaTransactionManager(emf);
        }

        @Bean
        public InMemoryGraphTestSupport.GraphState graphState() {
            return new InMemoryGraphTestSupport.GraphState();
        }

        @Bean(name = "neo4jTransactionManager")
        public PlatformTransactionManager neo4jTransactionManager(InMemoryGraphTestSupport.GraphState graphState) {
            return new InMemoryGraphTestSupport.GraphTransactionManager(graphState);
        }

        @Bean
        public ProductDao productDao(InMemoryGraphTestSupport.GraphState graphState) {
            return graphState.createProductDao();
        }

        @Bean
        public ProductPriceChangeEventDao eventDao(InMemoryGraphTestSupport.GraphState graphState) {
            return graphState.createEventDao();
        }

        @Bean
        public ProductSnapshotCodec productSnapshotCodec() {
            return new ProductSnapshotCodec();
        }

        @Bean
        public ProductUpdateGraphTransactionService productUpdateGraphTransactionService(
                ProductDao productDao,
                ProductPriceChangeEventDao eventDao,
                ProductSnapshotCodec snapshotCodec) {
            return new ProductUpdateGraphTransactionService(productDao, eventDao, snapshotCodec);
        }

        @Bean
        public AtomicBoolean failNextSqlProjection() {
            return new AtomicBoolean(false);
        }

        @Bean
        public AtomicBoolean failNextSqlCommit() { return new AtomicBoolean(false); }

        @Bean
        public ProductPriceAuditSqlWriter productPriceAuditSqlWriter(
                ProductPriceAuditRepository auditRepository,
                InMemoryGraphTestSupport.GraphState graphState,
                AtomicBoolean failNextSqlProjection, AtomicBoolean failNextSqlCommit) {
            return new ProductPriceAuditSqlWriter(auditRepository) {
                @Override
                public void projectAudit(ProductPriceChangeEvent event) {
                    if (graphState.isGraphTxActive()) {
                        throw new IllegalStateException("SQL projection must not execute inside a Neo4j transaction");
                    }
                    if (failNextSqlProjection.compareAndSet(true, false)) {
                        throw new RuntimeException("Simulated SQL projection failure");
                    }
                    super.projectAudit(event);
                    if (failNextSqlCommit.compareAndSet(true, false)) {
                        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                                new org.springframework.transaction.support.TransactionSynchronization() {
                                    @Override public void beforeCommit(boolean readOnly) {
                                        throw new IllegalStateException("injected SQL before-commit failure");
                                    }
                                });
                    }
                }
            };
        }

        @Bean
        public ProductPriceAuditProjector productPriceAuditProjector(
                ProductPriceAuditSqlWriter sqlWriter,
                ProductUpdateGraphTransactionService graphTxService,
                ProductPriceChangeEventDao eventDao) {
            return new ProductPriceAuditProjector(sqlWriter, graphTxService, eventDao, new com.own.product.config.ProductPriceAuditProperties(50, 30000));
        }

        @Bean
        public MerchantProductUpdateService merchantProductUpdateService(
                ProductUpdateGraphTransactionService graphTxService,
                ProductPriceAuditProjector projector) {
            return new MerchantProductUpdateService(graphTxService, projector);
        }

        @Bean
        public MerchantProductController merchantProductController(
                ProductDao productDao,
                ProductPriceAuditRepository auditRepository,
                MerchantProductUpdateService updateService) {
            return new MerchantProductController(productDao, auditRepository, updateService);
        }
    }

    @Autowired private InMemoryGraphTestSupport.GraphState graphState;
    @Autowired private ProductPriceAuditRepository auditRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private AtomicBoolean failNextSqlProjection;
    @Autowired private AtomicBoolean failNextSqlCommit;
    @Autowired private ProductUpdateGraphTransactionService graphTransactions;
    @Autowired private ProductSnapshotCodec snapshotCodec;
    @Autowired private ProductPriceAuditProjector projector;
    @Autowired private ProductPriceAuditSqlWriter sqlWriter;
    @Autowired private MerchantProductController controller;

    @BeforeEach
    void resetState() {
        graphState.clear();
        failNextSqlProjection.set(false);
        failNextSqlCommit.set(false);
        jdbcTemplate.execute("DELETE FROM prd_product_price_audit");
        Product initial = new Product();
        initial.setId(10L);
        initial.setPartyId(7L);
        initial.setName("Matcha");
        initial.setSkuCode("MAT-01");
        initial.setOriginalPrice("10.00");
        initial.setPromotionPrice("9.00");
        initial.setSaleStatus("AVAILABLE");
        graphState.putProduct(initial);
    }

    @Test
    void graphEventWriteFailureRollsBackProductUpdateAndRevision() {
        graphState.setFailNextInitialEventSave(true);
        MerchantProductCommand cmd = command("Matcha", "MAT-01", "12.50", "9.50", "cost increase");
        assertThrows(RuntimeException.class,
                () -> controller.update(10L, cmd, merchantRequest(7L, "idem-event-rollback")));

        Product after = graphState.getProduct(10L);
        assertEquals("10.00", after.getOriginalPrice());
        assertEquals("9.00", after.getPromotionPrice());
        assertNull(after.getPriceWriteRevision());
        assertTrue(graphState.allEvents().isEmpty());
        assertEquals(0L, auditRepository.count());
    }

    @Test
    void productSaveOrValidationFailureRollsBackGraphTransactionWithoutEventsOrSql() {
        MerchantProductCommand invalidCmd = command("Matcha", "MAT-01", "10.00", "15.00", "bad promo");
        assertThrows(TradeException.class,
                () -> controller.update(10L, invalidCmd, merchantRequest(7L, "idem-invalid")));
        assertNull(graphState.getProduct(10L).getPriceWriteRevision());
        assertTrue(graphState.allEvents().isEmpty());

        graphState.setFailNextProductSave(true);
        MerchantProductCommand validCmd = command("Matcha", "MAT-01", "11.00", "9.00", "adjust");
        assertThrows(RuntimeException.class,
                () -> controller.update(10L, validCmd, merchantRequest(7L, "idem-save-fail")));
        assertEquals("10.00", graphState.getProduct(10L).getOriginalPrice());
        assertNull(graphState.getProduct(10L).getPriceWriteRevision());
        assertTrue(graphState.allEvents().isEmpty());
        assertEquals(0L, auditRepository.count());
    }

    @Test
    void sqlProjectionFailurePreservesGraphFactAndSchedulerRecoversPendingEvent() {
        failNextSqlProjection.set(true);
        MerchantProductCommand cmd = command("Matcha Supreme", "MAT-01", "13.00", "11.00", "spring pricing");
        Product updated = (Product) controller.update(10L, cmd, merchantRequest(7L, "idem-sql-recover")).getData();
        assertEquals("13.00", updated.getOriginalPrice());
        assertEquals("13.00", graphState.getProduct(10L).getOriginalPrice());
        assertEquals(Long.valueOf(1L), graphState.getProduct(10L).getPriceWriteRevision());

        String eventKey = snapshotCodec.eventKey(7L, 10L, "idem-sql-recover");
        ProductPriceChangeEvent pending = graphState.getEvent(eventKey);
        assertNotNull(pending);
        assertFalse(pending.getProjected());
        assertEquals(0L, auditRepository.count());

        int recovered = projector.recoverPendingProjections();
        assertEquals(1, recovered);
        assertTrue(graphState.getEvent(eventKey).getProjected());
        assertEquals(1L, auditRepository.count());
    }

    @Test
    void sqlCommitFailureCannotMarkTheGraphEventProjected() {
        failNextSqlCommit.set(true);
        controller.update(10L, command("Matcha", "MAT-01", "13.00", "9.00", "test"), merchantRequest(7L, "sql-commit"));
        String key = snapshotCodec.eventKey(7L, 10L, "sql-commit");
        assertEquals("13.00", graphState.getProduct(10L).getOriginalPrice());
        assertFalse(graphState.getEvent(key).getProjected());
        assertEquals(0, auditRepository.count());
        assertEquals(1, projector.recoverPendingProjections());
        assertTrue(graphState.getEvent(key).getProjected());
        assertEquals(1, auditRepository.count());
    }

    @Test
    void saleStatusSharesTheProductLockWithoutRevertingThePrice() {
        controller.update(10L, command("Matcha", "MAT-01", "13.00", "9.00", "test"), merchantRequest(7L, "new-price"));
        controller.saleStatus(10L, "OFF_SHELF", merchantRequest(7L, "status"));
        assertEquals("13.00", graphState.getProduct(10L).getOriginalPrice());
        assertEquals("OFF_SHELF", graphState.getProduct(10L).getSaleStatus());
        assertEquals(2L, graphState.getProduct(10L).getPriceWriteRevision());
        assertEquals(1, auditRepository.count());
        assertThrows(TradeException.class, () -> graphTransactions.changeSaleStatus(10L, 8L, "AVAILABLE"));
        assertEquals("OFF_SHELF", graphState.getProduct(10L).getSaleStatus());
    }

    @Test
    void sqlSuccessFollowedByGraphMarkFailureDeduplicatesOnReplayAndRecovery() {
        graphState.setFailNextMarkProjectedSave(true);
        MerchantProductCommand cmd = command("Matcha", "MAT-01", "14.00", "12.00", "new tariff");
        controller.update(10L, cmd, merchantRequest(7L, "idem-ack-fail"));

        String eventKey = snapshotCodec.eventKey(7L, 10L, "idem-ack-fail");
        assertEquals(1L, auditRepository.count());
        assertFalse(graphState.getEvent(eventKey).getProjected());

        Product replayed = (Product) controller.update(10L, cmd, merchantRequest(7L, "idem-ack-fail")).getData();
        assertEquals("14.00", replayed.getOriginalPrice());
        assertEquals(1L, auditRepository.count());
        assertTrue(graphState.getEvent(eventKey).getProjected());

        assertEquals(0, projector.recoverPendingProjections());
        assertEquals(1L, auditRepository.count());
    }

    @Test
    void immutableFieldMismatchOnExistingEventKeyIsRejectedDuringSqlProjection() {
        MerchantProductCommand cmd = command("Matcha", "MAT-01", "14.00", "12.00", "audit check");
        controller.update(10L, cmd, merchantRequest(7L, "idem-tamper"));
        String eventKey = snapshotCodec.eventKey(7L, 10L, "idem-tamper");

        jdbcTemplate.update(
                "UPDATE prd_product_price_audit SET original_price_after = 99.99 WHERE event_key = ?",
                eventKey);
        ProductPriceChangeEvent event = graphState.getEvent(eventKey);
        assertThrows(IllegalStateException.class, () -> sqlWriter.projectAudit(event));
    }

    @Test
    void idempotentReplayReturnsOriginalSnapshotWhileDifferentPayloadReturns409Conflict() {
        MerchantProductCommand first = command("Matcha V1", "MAT-01", "12.00", "10.00", "first change");
        Product firstResp = (Product) controller.update(10L, first, merchantRequest(7L, "idem-key-1")).getData();
        assertEquals("Matcha V1", firstResp.getName());
        assertEquals("12.00", firstResp.getOriginalPrice());

        MerchantProductCommand second = command("Matcha V2", "MAT-01", "16.00", "14.00", "second change");
        controller.update(10L, second, merchantRequest(7L, "idem-key-2"));
        assertEquals("16.00", graphState.getProduct(10L).getOriginalPrice());
        assertEquals(Long.valueOf(2L), graphState.getProduct(10L).getPriceWriteRevision());

        Product replayFirst = (Product) controller.update(10L, first, merchantRequest(7L, "idem-key-1")).getData();
        assertEquals("Matcha V1", replayFirst.getName());
        assertEquals("12.00", replayFirst.getOriginalPrice());
        assertEquals("16.00", graphState.getProduct(10L).getOriginalPrice());
        assertEquals(2L, auditRepository.count());

        MerchantProductCommand conflicting = command("Matcha Conflict", "MAT-01", "18.00", "14.00", "conflict");
        TradeException conflict = assertThrows(
                TradeException.class,
                () -> controller.update(10L, conflicting, merchantRequest(7L, "idem-key-1")));
        assertEquals(409, conflict.getStatus());
        assertEquals("16.00", graphState.getProduct(10L).getOriginalPrice());
        assertEquals(2L, auditRepository.count());
    }

    @Test
    void nonPriceUpdateSavesReplayEventWithoutGeneratingSqlAudit() {
        MerchantProductCommand nonPrice = command("Matcha Renamed", "MAT-01", "10.00", "9.00", "");
        Product updated = (Product) controller.update(10L, nonPrice, merchantRequest(7L, "idem-no-price")).getData();
        assertEquals("Matcha Renamed", updated.getName());
        String eventKey = snapshotCodec.eventKey(7L, 10L, "idem-no-price");
        ProductPriceChangeEvent event = graphState.getEvent(eventKey);
        assertNotNull(event);
        assertFalse(event.getPriceChanged());
        assertTrue(event.getProjected());
        assertEquals(0L, auditRepository.count());
    }

    @Test
    @SuppressWarnings("unchecked")
    void concurrentProductPriceUpdatesSerializeOnWriteLockAndCoexistWithNullEventKeyHistory() throws Exception {
        jdbcTemplate.update(
                "INSERT INTO prd_product_price_audit "
                        + "(event_key, product_id, merchant_id, original_price_before, original_price_after, "
                        + "promotion_price_before, promotion_price_after, reason, changed_at) "
                        + "VALUES (NULL, 10, 7, 8.00, 9.00, NULL, NULL, 'legacy-1', '2026-01-01 00:00:00'), "
                        + "(NULL, 10, 7, 9.00, 10.00, NULL, NULL, 'legacy-2', '2026-01-02 00:00:00')");

        CountDownLatch firstEnteredLock = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicBoolean firstHookRun = new AtomicBoolean(false);
        graphState.setAfterLockHook(id -> {
            if (firstHookRun.compareAndSet(false, true)) {
                firstEnteredLock.countDown();
                try {
                    releaseFirst.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Callable<Product> task1 = () -> (Product) controller.update(
                    10L,
                    command("Matcha", "MAT-01", "12.00", "9.00", "step-1"),
                    merchantRequest(7L, "idem-concurrent-1")).getData();
            Callable<Product> task2 = () -> {
                assertTrue(firstEnteredLock.await(5, TimeUnit.SECONDS));
                releaseFirst.countDown();
                return (Product) controller.update(
                        10L,
                        command("Matcha", "MAT-01", "15.00", "9.00", "step-2"),
                        merchantRequest(7L, "idem-concurrent-2")).getData();
            };

            Future<Product> f1 = pool.submit(task1);
            Future<Product> f2 = pool.submit(task2);
            f1.get(5, TimeUnit.SECONDS);
            f2.get(5, TimeUnit.SECONDS);
        } finally {
            graphState.setAfterLockHook(null);
            pool.shutdownNow();
        }

        Product finalProduct = graphState.getProduct(10L);
        assertEquals("15.00", finalProduct.getOriginalPrice());
        assertEquals(Long.valueOf(2L), finalProduct.getPriceWriteRevision());

        ProductPriceChangeEvent ev2 = graphState.getEvent(snapshotCodec.eventKey(7L, 10L, "idem-concurrent-2"));
        assertEquals("12.00", ev2.getOriginalPriceBefore());
        assertEquals("15.00", ev2.getOriginalPriceAfter());

        Page<ProductPriceAudit> page = auditRepository.findByProductIdAndMerchantIdOrderByIdDesc(
                10L, 7L, PageRequest.of(0, 20));
        assertEquals(4L, page.getTotalElements());
        Map<String, Object> auditResp = (Map<String, Object>) controller.priceAudits(
                10L, 0, 20, merchantRequest(7L, "idem-read")).getData();
        List<ProductPriceAudit> items = (List<ProductPriceAudit>) auditResp.get("items");
        assertEquals(4, items.size());
    }

    private MerchantProductCommand command(
            String name, String sku, String originalPrice, String promotionPrice, String reason) {
        MerchantProductCommand c = new MerchantProductCommand();
        c.setPartyId(7L);
        c.setName(name);
        c.setSkuCode(sku);
        c.setOriginalPrice(originalPrice);
        c.setPromotionPrice(promotionPrice);
        c.setPriceChangeReason(reason);
        return c;
    }

    private MockHttpServletRequest merchantRequest(Long merchantId, String idempotencyKey) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Actor-Id", String.valueOf(merchantId));
        request.addHeader("X-Actor-Type", "MERCHANT");
        request.addHeader("Idempotency-Key", idempotencyKey);
        return request;
    }
}
