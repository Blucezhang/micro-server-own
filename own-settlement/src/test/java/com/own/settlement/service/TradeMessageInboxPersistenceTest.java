package com.own.settlement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.face.trade.TradeException;
import com.own.face.trade.inbox.TradeMessageInboxRepository;
import com.own.face.trade.inbox.TradeMessageInboxService;
import jakarta.persistence.EntityManagerFactory;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import static org.junit.jupiter.api.Assertions.*;

class TradeMessageInboxPersistenceTest {
    private EntityManagerFactory factory;
    private TradeMessageInboxRepository repository;
    private TradeMessageInboxService inbox;
    private JdbcTemplate jdbc;

    @BeforeEach
    void isolatedDatabase() {
        var source = new DriverManagerDataSource("jdbc:h2:mem:inbox_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        var builder = new LocalContainerEntityManagerFactoryBean();
        builder.setDataSource(source); builder.setPackagesToScan("com.own.face.trade.inbox");
        builder.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        builder.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
        builder.afterPropertiesSet(); factory = builder.getObject();
        var manager = new JpaTransactionManager(factory);
        repository = new JpaRepositoryFactory(SharedEntityManagerCreator.createSharedEntityManager(factory))
                .getRepository(TradeMessageInboxRepository.class);
        ProxyFactory proxy = new ProxyFactory(new TradeMessageInboxService(repository, new ObjectMapper()));
        proxy.addAdvice(new TransactionInterceptor(manager, new AnnotationTransactionAttributeSource()));
        inbox = (TradeMessageInboxService) proxy.getProxy();
        jdbc = new JdbcTemplate(source);
        jdbc.execute("CREATE TABLE test_business_effect (operation_id VARCHAR(64) PRIMARY KEY)");
    }

    @AfterEach
    void close() { if (factory != null) factory.close(); }

    @Test
    void businessFailureRollsBackDeduplicationAndActualSqlWriteForRedelivery() {
        Map<String, Object> payload = Map.of("orderNo", "ORD-1", "eventType", "PAYMENT_SUCCEEDED");
        assertThrows(IllegalStateException.class, () -> inbox.consume("settlement-saga", 1L, payload, () -> {
            effect("payment"); throw new IllegalStateException("business failed");
        }));
        assertEquals(0, repository.count()); assertEquals(0, effects());
        assertTrue(inbox.consume("settlement-saga", 1L, payload, () -> effect("payment")));
        assertEquals(1, repository.count()); assertEquals(1, effects());
        assertFalse(inbox.consume("settlement-saga", 1L, payload, () -> fail("duplicate handler")));
    }

    @Test
    void commitFailureCannotLeaveAConsumedMarkerWithoutBusinessEffect() {
        assertThrows(IllegalStateException.class, () -> inbox.consume("consumer", 1L, Map.of("a", 1), () -> {
            effect("payment");
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void beforeCommit(boolean readOnly) { throw new IllegalStateException("commit failed"); }
            });
        }));
        assertEquals(0, repository.count()); assertEquals(0, effects());
        assertTrue(inbox.consume("consumer", 1L, Map.of("a", 1), () -> effect("payment")));
    }

    @Test
    void sameIdWithDifferentPayloadCannotSilentlySkipAChangedCommand() {
        inbox.consume("consumer", 1L, Map.of("amount", 10), () -> effect("payment"));
        assertThrows(TradeException.class, () -> inbox.consume("consumer", 1L, Map.of("amount", 20), () -> effect("other")));
        assertEquals(1, effects());
    }

    @Test
    void reorderedNestedMapsHaveTheSameFingerprint() {
        var first = new java.util.LinkedHashMap<String, Object>(); first.put("a", 1); first.put("b", 2);
        var second = new java.util.LinkedHashMap<String, Object>(); second.put("b", 2); second.put("a", 1);
        inbox.consume("consumer", 1L, Map.of("body", first), () -> effect("payment"));
        assertFalse(inbox.consume("consumer", 1L, Map.of("body", second), () -> fail("map order is not a new event")));
    }

    @Test
    void differentConsumersHaveIndependentDeduplicationScopes() {
        assertTrue(inbox.consume("inventory", 1L, Map.of("a", 1), () -> effect("inventory")));
        assertTrue(inbox.consume("promotion", 1L, Map.of("a", 1), () -> effect("promotion")));
        assertEquals(2, repository.count()); assertEquals(2, effects());
    }

    @Test
    void unrelatedDatabaseFailureIsNotHiddenByInsertIgnore() {
        assertThrows(RuntimeException.class, () -> inbox.consume("consumer", 1L, Map.of("a", 1), () -> {
            effect("same"); effect("same");
        }));
        assertEquals(0, repository.count()); assertEquals(0, effects());
    }

    private void effect(String id) { jdbc.update("INSERT INTO test_business_effect VALUES (?)", id); }
    private int effects() { return jdbc.queryForObject("SELECT COUNT(*) FROM test_business_effect", Integer.class); }
}
