package com.own.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.face.trade.TradeException;
import com.own.face.util.Resp;
import com.own.product.OwnProductApplication;
import com.own.product.audit.domain.ProductPriceAudit;
import com.own.product.audit.repository.ProductPriceAuditRepository;
import com.own.product.dao.ProductDao;
import com.own.product.dao.ProductPriceChangeEventDao;
import com.own.product.domain.Product;
import com.own.product.domain.ProductPriceChangeEvent;
import com.own.product.dto.MerchantProductCommand;
import com.own.product.dto.PublicProductView;
import com.own.product.service.MerchantProductUpdateService;
import com.own.product.service.ProductPriceAuditProjector;
import com.own.product.service.ProductPriceAuditSqlWriter;
import com.own.product.service.ProductSnapshotCodec;
import com.own.product.service.ProductUpdateGraphTransactionService;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MerchantProductControllerTest {

    private MerchantProductController newController(ProductDao dao) {
        ProductPriceAuditRepository audits = mock(ProductPriceAuditRepository.class);
        MerchantProductUpdateService updateService = mock(MerchantProductUpdateService.class);
        return new MerchantProductController(dao, audits, updateService);
    }

    @Test
    public void constructorRejectsNullDependenciesWithoutBypassPaths() {
        ProductDao dao = mock(ProductDao.class);
        ProductPriceAuditRepository audits = mock(ProductPriceAuditRepository.class);
        MerchantProductUpdateService updateService = mock(MerchantProductUpdateService.class);
        assertThrows(NullPointerException.class, () -> new MerchantProductController(null, audits, updateService));
        assertThrows(NullPointerException.class, () -> new MerchantProductController(dao, null, updateService));
        assertThrows(NullPointerException.class, () -> new MerchantProductController(dao, audits, null));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void browseUsesStableAscendingProductIdPaginationWithoutInternalStock() {
        ProductDao dao = mock(ProductDao.class);
        Product two = product(2L, "Tea"), one = product(1L, "Tea"), off = product(3L, "Tea");
        two.setStocksNum("99");
        off.setSaleStatus("OFF_SHELF");
        when(dao.queryProduct()).thenReturn(Arrays.asList(two, off, one));
        MerchantProductController controller = newController(dao);
        Resp response = controller.browse(null, null, "tea", 0, 20);
        Map<String, Object> data = (Map<String, Object>) response.getData();
        List<PublicProductView> products = (List<PublicProductView>) data.get("items");
        assertEquals(2, products.size());
        assertEquals(Long.valueOf(1L), products.get(0).getId());
        assertEquals(Long.valueOf(2L), products.get(1).getId());
        assertFalse(Arrays.asList(products.get(1).getClass().getDeclaredFields()).toString().contains("stocksNum"));
    }

    @Test
    public void publicDetailReturnsAvailableProductOnly() {
        ProductDao dao = mock(ProductDao.class);
        Product available = product(1L, "Tea");
        available.setStocksNum("99");
        when(dao.queryProductById(1L)).thenReturn(available);
        MerchantProductController controller = newController(dao);
        PublicProductView view = (PublicProductView) controller.detail(1L).getData();
        assertEquals(Long.valueOf(1L), view.getId());
        assertFalse(Arrays.asList(view.getClass().getDeclaredFields()).toString().contains("stocksNum"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void merchantCatalogIncludesOnlyOwnOffShelfProducts() {
        ProductDao dao = mock(ProductDao.class);
        Product mine = product(1L, "Tea"), other = product(2L, "Coffee");
        mine.setPartyId(7L);
        mine.setSaleStatus("OFF_SHELF");
        other.setPartyId(8L);
        when(dao.queryProduct()).thenReturn(Arrays.asList(other, mine));
        MerchantProductController controller = newController(dao);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Actor-Id", "7");
        request.addHeader("X-Actor-Type", "MERCHANT");
        Resp response = controller.mine(null, null, "OFF_SHELF", 0, 20, request);
        List<Product> products = (List<Product>) ((Map<String, Object>) response.getData()).get("items");
        assertEquals(1, products.size());
        assertEquals(Long.valueOf(1L), products.get(0).getId());
    }

    @Test
    public void merchantPriceUpdateDelegatesToSingleSourceUpdateServiceAndProjectsAudit() {
        ProductDao dao = mock(ProductDao.class);
        ProductPriceChangeEventDao eventDao = mock(ProductPriceChangeEventDao.class);
        ProductPriceAuditRepository audits = mock(ProductPriceAuditRepository.class);
        Product item = product(3L, "Tea");
        item.setPartyId(7L);
        item.setOriginalPrice("10.00");
        item.setPriceWriteRevision(1L);
        when(dao.lockProductById(3L)).thenReturn(item);
        when(dao.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventDao.save(any(ProductPriceChangeEvent.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventDao.findByEventKey(anyString())).thenReturn(null).thenAnswer(inv -> {
            ProductPriceChangeEvent stored = new ProductPriceChangeEvent();
            stored.setEventKey(inv.getArgument(0));
            stored.setProjected(false);
            return stored;
        });
        when(audits.findByEventKey(anyString())).thenAnswer(inv -> Optional.of(new ProductPriceAudit(
                inv.getArgument(0),
                3L,
                7L,
                new BigDecimal("10.00"),
                new BigDecimal("12.00"),
                null,
                null,
                "seasonal update",
                new Date())));

        ProductSnapshotCodec codec = new ProductSnapshotCodec();
        ProductUpdateGraphTransactionService graphTx = new ProductUpdateGraphTransactionService(dao, eventDao, codec);
        ProductPriceAuditSqlWriter sqlWriter = new ProductPriceAuditSqlWriter(audits);
        ProductPriceAuditProjector projector = new ProductPriceAuditProjector(sqlWriter, graphTx, eventDao, new com.own.product.config.ProductPriceAuditProperties(50, 30000));
        MerchantProductUpdateService updateService = new MerchantProductUpdateService(graphTx, projector);
        MerchantProductController controller = new MerchantProductController(dao, audits, updateService);

        MerchantProductCommand command = new MerchantProductCommand();
        command.setName("Tea");
        command.setSkuCode("TEA-1");
        command.setOriginalPrice("12.00");
        command.setPriceChangeReason("seasonal update");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Actor-Id", "7");
        request.addHeader("X-Actor-Type", "MERCHANT");
        request.addHeader("Idempotency-Key", "test-product-audit");

        Resp resp = controller.update(3L, command, request);
        Product returned = (Product) resp.getData();
        assertEquals("12.00", returned.getOriginalPrice());
        verify(dao).lockProductById(3L);
        verify(audits).insertOnDuplicateKeyDoNothing(
                anyString(),
                eq(3L),
                eq(7L),
                eq(new BigDecimal("10.00")),
                eq(new BigDecimal("12.00")),
                isNull(),
                isNull(),
                eq("seasonal update"),
                any(Date.class));
        verify(audits, never()).save(any(ProductPriceAudit.class));
    }

    @Test
    public void productPriceWriteRevisionIsHiddenFromJsonAndLockQueryUsesCoalesce() throws Exception {
        Product item = product(5L, "Green Tea");
        item.setOriginalPrice("18.00");
        item.setPriceWriteRevision(9L);
        String json = new ObjectMapper().writeValueAsString(item);
        assertFalse(json.contains("priceWriteRevision"));

        Method lockMethod = ProductDao.class.getMethod("lockProductById", Long.class);
        Query query = lockMethod.getAnnotation(Query.class);
        assertNotNull(query);
        assertTrue(query.value().contains("SET p.priceWriteRevision=coalesce(p.priceWriteRevision,0)+1 RETURN p"));

        EnableNeo4jRepositories neo4jRepos = OwnProductApplication.class.getAnnotation(EnableNeo4jRepositories.class);
        assertNotNull(neo4jRepos);
        assertEquals("neo4jTransactionManager", neo4jRepos.transactionManagerRef());
        assertNotNull(OwnProductApplication.class.getAnnotation(EnableScheduling.class));
    }

    @Test
    public void merchantCannotSetPromotionPriceAboveOriginalPrice() {
        ProductDao dao = mock(ProductDao.class);
        MerchantProductController controller = newController(dao);
        MerchantProductCommand command = new MerchantProductCommand();
        command.setPartyId(1L);
        command.setName("Tea");
        command.setSkuCode("TEA-1");
        command.setOriginalPrice("10.00");
        command.setPromotionPrice("10.01");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Actor-Id", "1");
        request.addHeader("X-Actor-Type", "MERCHANT");
        request.addHeader("Idempotency-Key", "test-product-price");
        assertThrows(TradeException.class, () -> controller.create(command, request));
    }

    @Test
    public void merchantCannotPublishSubCentPrice() {
        ProductDao dao = mock(ProductDao.class);
        MerchantProductController controller = newController(dao);
        MerchantProductCommand command = new MerchantProductCommand();
        command.setPartyId(1L);
        command.setName("Tea");
        command.setSkuCode("TEA-1");
        command.setOriginalPrice("10.001");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Actor-Id", "1");
        request.addHeader("X-Actor-Type", "MERCHANT");
        request.addHeader("Idempotency-Key", "test-product-sub-cent");
        TradeException error = assertThrows(TradeException.class, () -> controller.create(command, request));
        assertEquals(422, error.getStatus());
        command.setOriginalPrice("10.00");
        command.setPromotionPrice("9.999");
        error = assertThrows(TradeException.class, () -> controller.create(command, request));
        assertEquals(422, error.getStatus());
        verify(dao, never()).save(any(Product.class));
    }

    @Test
    public void missingLegacyProductUsesNotFoundInsteadOfNullPointer() {
        ProductDao dao = mock(ProductDao.class);
        ProductController controller = new ProductController();
        try {
            Field field = ProductController.class.getDeclaredField("productDao");
            field.setAccessible(true);
            field.set(controller, dao);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
        assertThrows(TradeException.class, () -> controller.getProduct(999L));
    }

    @Test
    public void browseRejectsPageMultiplicationOverflow() {
        ProductDao dao = mock(ProductDao.class);
        MerchantProductController controller = newController(dao);
        assertThrows(TradeException.class, () -> controller.browse(null, null, null, Integer.MAX_VALUE, 2));
    }

    @Test
    public void legacyQueryRejectsCypherSyntaxInIdentifiers() {
        ProductDao dao = mock(ProductDao.class);
        ProductController controller = new ProductController();
        try {
            Field field = ProductController.class.getDeclaredField("productDao");
            field.setAccessible(true);
            field.set(controller, dao);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("partyId", "1}) MATCH (n) DETACH DELETE n //");
        assertThrows(TradeException.class, () -> controller.queryProduct(values));
    }

    private Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setSaleStatus("AVAILABLE");
        return product;
    }
}
