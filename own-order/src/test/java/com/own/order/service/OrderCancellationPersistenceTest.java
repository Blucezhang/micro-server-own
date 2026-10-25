package com.own.order.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.order.domain.*;
import com.own.order.repository.*;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.*;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderCancellationPersistenceTest {
    private EntityManagerFactory entityManagerFactory;
    private JpaTransactionManager manager;
    private TransactionTemplate transactions;
    private TradeOrderRepository orders;
    private TradeSubOrderRepository children;
    private OrderItemRepository items;
    private OrderEventRepository events;
    private InventoryClient inventory;
    private CouponClient coupons;

    @BeforeEach
    void isolatedDatabase() {
        var factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(new DriverManagerDataSource("jdbc:h2:mem:cancel_" + UUID.randomUUID()
                + ";DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000", "sa", ""));
        factory.setPackagesToScan("com.own.order.domain");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
        factory.afterPropertiesSet(); entityManagerFactory = factory.getObject();
        manager = new JpaTransactionManager(entityManagerFactory); transactions = new TransactionTemplate(manager);
        var repositories = new JpaRepositoryFactory(SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory));
        orders = repositories.getRepository(TradeOrderRepository.class);
        children = repositories.getRepository(TradeSubOrderRepository.class);
        items = repositories.getRepository(OrderItemRepository.class);
        events = repositories.getRepository(OrderEventRepository.class);
        inventory = mock(InventoryClient.class); coupons = mock(CouponClient.class);
        transactions.executeWithoutResult(status -> {
            orders.save(new TradeOrder("ORD-1", 7L, BigDecimal.TEN, BigDecimal.ZERO, "test", "key"));
            children.save(new TradeSubOrder("SUB-1", "ORD-1", 2L, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO));
        });
    }

    @AfterEach
    void closeDatabase() { if (entityManagerFactory != null) entityManagerFactory.close(); }

    @Test
    void partialReleaseFailureKeepsBarrierAndFreshWorkerCompletesOnce() {
        doThrow(new IllegalStateException("coupon release response lost")).when(coupons).release("ORD-1");
        assertThrows(IllegalStateException.class, () -> service(3, events).tryCancel(buyer(), "ORD-1", null));
        TradeOrder pending = orders.findByOrderNo("ORD-1");
        assertEquals(OrderStatus.PENDING_PAYMENT, pending.getStatus());
        assertTrue(pending.isCancellationStarted());
        assertEquals("PENDING", pending.getCancellationRecoveryStatus());
        assertThrows(TradeException.class, () -> proxy(new OrderPaymentGuard(orders, 15, new com.own.face.trade.config.TradeRecoveryProperties(3, 30000, 30000))).claim("ORD-1"));
        assertFalse(orders.findByOrderNo("ORD-1").isPaymentStarted());
        doNothing().when(coupons).release("ORD-1");

        var worker = new OrderCancellationRecoveryScheduler(orders, service(3, events));
        worker.recover(); worker.recover();

        assertEquals(OrderStatus.CANCELED, orders.findByOrderNo("ORD-1").getStatus());
        assertEquals("COMPLETED", orders.findByOrderNo("ORD-1").getCancellationRecoveryStatus());
        assertEquals(SubOrderStatus.CANCELED, children.findBySubOrderNo("SUB-1").getStatus());
        assertEquals(1, events.count());
        verify(inventory, times(1)).release("ORD-1");
        verify(inventory, times(1)).reconcileRelease(eq("ORD-1"), anyList());
        verify(coupons, times(2)).release("ORD-1");
    }

    @Test
    void actualCommitFailureRollsBackCancellationAndEventButNotIntent() {
        doAnswer(call -> {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void beforeCommit(boolean readOnly) { throw new IllegalStateException("commit failed"); }
            });
            return null;
        }).when(inventory).release("ORD-1");

        assertThrows(IllegalStateException.class, () -> service(3, events).tryCancel(buyer(), "ORD-1", null));

        assertEquals(OrderStatus.PENDING_PAYMENT, orders.findByOrderNo("ORD-1").getStatus());
        assertEquals(SubOrderStatus.PENDING_PAYMENT, children.findBySubOrderNo("SUB-1").getStatus());
        assertEquals("PENDING", orders.findByOrderNo("ORD-1").getCancellationRecoveryStatus());
        assertEquals(0, events.count());
        service(3, events).tryCancel(buyer(), "ORD-1", null);
        assertEquals(OrderStatus.CANCELED, orders.findByOrderNo("ORD-1").getStatus());
        assertEquals(1, events.count());
    }

    @Test
    void localWriteFailureRetainsIndependentIntent() {
        OrderEventRepository failingEvents = mock(OrderEventRepository.class);
        when(failingEvents.save(any())).thenThrow(new IllegalStateException("event insert failed"));
        assertThrows(IllegalStateException.class, () -> service(3, failingEvents).tryCancel(buyer(), "ORD-1", null));
        assertEquals(OrderStatus.PENDING_PAYMENT, orders.findByOrderNo("ORD-1").getStatus());
        assertTrue(orders.findByOrderNo("ORD-1").isCancellationStarted());
        service(3, events).tryCancel(buyer(), "ORD-1", null);
        assertEquals(1, events.count());
    }

    @Test
    void retryLimitPersistsManualStateAndDoesNotUnlockWhenLimitIsRaised() {
        doThrow(new IllegalStateException("release uncertain")).when(coupons).release("ORD-1");
        var service = service(2, events);
        assertThrows(IllegalStateException.class, () -> service.tryCancel(buyer(), "ORD-1", null));
        assertThrows(IllegalStateException.class, () -> service.tryCancel(buyer(), "ORD-1", null));
        assertThrows(TradeException.class, () -> service.tryCancel(buyer(), "ORD-1", null));
        assertEquals("RECONCILIATION_REQUIRED", orders.findByOrderNo("ORD-1").getCancellationRecoveryStatus());
        assertThrows(TradeException.class, () -> service(9, events).tryCancel(buyer(), "ORD-1", null));
        new OrderCancellationRecoveryScheduler(orders, service).recover();
        verify(coupons, times(2)).release("ORD-1");
        assertEquals(0, events.count());
    }

    @Test
    void unauthorizedAndNotYetExpiredRequestsDoNotCreateIntent() {
        var service = service(3, events);
        assertThrows(TradeException.class, () -> service.tryCancel(new TradeActor(8L, ActorType.BUYER), "ORD-1", null));
        assertThrows(TradeException.class, () -> service.tryCancel(new TradeActor(7L, ActorType.MERCHANT), "ORD-1", null));
        assertNull(service.tryCancel(system(), "ORD-1", new Date(0)));
        assertFalse(orders.findByOrderNo("ORD-1").isCancellationStarted());
        verifyNoInteractions(inventory, coupons);
    }

    @Test
    void paymentOwnershipPreventsCancellation() {
        assertTrue(proxy(new OrderPaymentGuard(orders, 15, new com.own.face.trade.config.TradeRecoveryProperties(3, 30000, 30000))).claim("ORD-1"));
        assertNull(service(3, events).tryCancel(buyer(), "ORD-1", null));
        assertFalse(orders.findByOrderNo("ORD-1").isCancellationStarted());
        verifyNoInteractions(inventory, coupons);
    }

    @Test
    void expiredCancellationKeepsItsOriginalReasonWhenWorkerResumes() {
        doThrow(new IllegalStateException("release failed")).when(coupons).release("ORD-1");
        assertThrows(IllegalStateException.class, () -> service(3, events).tryCancel(system(), "ORD-1",
                new Date(System.currentTimeMillis() + 60_000)));
        doNothing().when(coupons).release("ORD-1");
        new OrderCancellationRecoveryScheduler(orders, service(3, events)).recover();
        OrderEvent event = events.findByOrderNoOrderByIdAsc("ORD-1").get(0);
        assertEquals("ORDER_EXPIRED", event.getEventType());
        assertEquals("SYSTEM", event.getActorType());
        assertEquals(0L, event.getActorId());
    }

    @Test
    void waitingPaymentCannotWinAfterRemoteReleaseAndLocalRollback() throws Exception {
        CountDownLatch releaseEntered = new CountDownLatch(1), continueRelease = new CountDownLatch(1);
        doAnswer(call -> {
            releaseEntered.countDown();
            if (!continueRelease.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("test release timed out");
            return null;
        }).when(inventory).release("ORD-1");
        doThrow(new IllegalStateException("coupon failed")).when(coupons).release("ORD-1");
        ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            var cancellation = workers.submit(() -> service(3, events).tryCancel(buyer(), "ORD-1", null));
            assertTrue(releaseEntered.await(5, TimeUnit.SECONDS));
            var payment = workers.submit(() -> proxy(new OrderPaymentGuard(orders, 15, new com.own.face.trade.config.TradeRecoveryProperties(3, 30000, 30000))).claim("ORD-1"));
            continueRelease.countDown();
            assertInstanceOf(IllegalStateException.class, assertThrows(ExecutionException.class,
                    () -> cancellation.get(5, TimeUnit.SECONDS)).getCause());
            assertInstanceOf(TradeException.class, assertThrows(ExecutionException.class,
                    () -> payment.get(5, TimeUnit.SECONDS)).getCause());
            assertTrue(orders.findByOrderNo("ORD-1").isCancellationStarted());
            assertFalse(orders.findByOrderNo("ORD-1").isPaymentStarted());
        } finally { continueRelease.countDown(); workers.shutdownNow(); }
    }

    private OrderCancellationService service(int limit, OrderEventRepository eventRepository) {
        return proxy(new OrderCancellationService(proxy(new OrderCancellationGuard(orders, new com.own.face.trade.config.TradeRecoveryProperties(limit, 30000, 30000))), orders,
                children, items, eventRepository, inventory, coupons));
    }
    private TradeActor buyer() { return new TradeActor(7L, ActorType.BUYER); }
    private TradeActor system() { return new TradeActor(0L, ActorType.SYSTEM); }
    @SuppressWarnings("unchecked")
    private <T> T proxy(T target) {
        ProxyFactory factory = new ProxyFactory(target);
        factory.addAdvice(new TransactionInterceptor(manager, new AnnotationTransactionAttributeSource()));
        return (T) factory.getProxy();
    }
}
