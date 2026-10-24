package com.own.order.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.order.domain.OrderStatus;
import com.own.order.domain.TradeOrder;
import com.own.order.repository.*;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.TransactionTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderPaymentRecoveryPersistenceTest {
    private EntityManagerFactory entityManagerFactory;
    private JpaTransactionManager manager;
    private TransactionTemplate transactions;
    private JpaRepositoryFactory repositories;
    private TradeOrderRepository orders;
    private InventoryClient inventory;
    private CouponClient coupons;
    private OrderPaymentGuard guard;

    @BeforeEach
    void isolatedDatabase() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(new DriverManagerDataSource("jdbc:h2:mem:order_recovery_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", ""));
        factory.setPackagesToScan("com.own.order.domain");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
        factory.afterPropertiesSet();
        entityManagerFactory = factory.getObject();
        manager = new JpaTransactionManager(entityManagerFactory);
        transactions = new TransactionTemplate(manager);
        repositories = new JpaRepositoryFactory(SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory));
        orders = repositories.getRepository(TradeOrderRepository.class);
        guard = proxy(new OrderPaymentGuard(orders, 15, 2));
        inventory = mock(InventoryClient.class); coupons = mock(CouponClient.class);
        transactions.executeWithoutResult(status -> orders.save(new TradeOrder("ORD-1", 7L, BigDecimal.TEN, BigDecimal.ONE, "test", "key")));
    }

    @AfterEach
    void closeDatabase() { if (entityManagerFactory != null) entityManagerFactory.close(); }

    @Test
    void couponFailureLeavesDurableBarrierAndFreshWorkerResumesInsteadOfCanceling() {
        doThrow(new IllegalStateException("coupon unavailable after inventory committed"))
                .when(coupons).consume(eq("ORD-1"), any(BigDecimal.class));
        OrderService initial = service(repositories.getRepository(OrderEventRepository.class));

        assertThrows(IllegalStateException.class, () -> initial.paymentSucceeded("ORD-1", system()));

        TradeOrder pending = orders.findByOrderNo("ORD-1");
        assertEquals(OrderStatus.PENDING_PAYMENT, pending.getStatus());
        assertTrue(pending.isPaymentStarted());
        assertEquals("PENDING", pending.getPaymentRecoveryStatus());
        assertThrows(TradeException.class, () -> initial.cancel(new TradeActor(7L, ActorType.BUYER), "ORD-1"));
        assertFalse(initial.expirePendingOrder("ORD-1", new Date(System.currentTimeMillis() + 60_000)));
        verify(inventory, never()).release(anyString());
        verify(coupons, never()).release(anyString());

        OrderService restarted = service(repositories.getRepository(OrderEventRepository.class));
        new OrderPaymentRecoveryScheduler(orders, restarted).recover();

        assertEquals(OrderStatus.PAID, orders.findByOrderNo("ORD-1").getStatus());
        assertEquals("COMPLETED", orders.findByOrderNo("ORD-1").getPaymentRecoveryStatus());
        verify(inventory, times(1)).commit(eq("ORD-1"), anyList());
        verify(inventory, times(1)).reconcileCommit(eq("ORD-1"), anyList());
        verify(coupons, times(1)).reconcileConsume("ORD-1", 7L, new BigDecimal("1.00"));
        restarted.paymentSucceeded("ORD-1", system());
        verify(inventory, times(1)).reconcileCommit(eq("ORD-1"), anyList());
    }

    @Test
    void localOrderAndSubOrderCompletionRollbackDoesNotEraseStartedIntent() {
        OrderEventRepository events = mock(OrderEventRepository.class);
        when(events.save(any())).thenThrow(new IllegalStateException("injected event insert failure"));

        assertThrows(IllegalStateException.class, () -> service(events).paymentSucceeded("ORD-1", system()));

        TradeOrder pending = orders.findByOrderNo("ORD-1");
        assertEquals(OrderStatus.PENDING_PAYMENT, pending.getStatus());
        assertTrue(pending.isPaymentStarted());
        assertEquals("PENDING", pending.getPaymentRecoveryStatus());
        service(repositories.getRepository(OrderEventRepository.class)).paymentSucceeded("ORD-1", system());
        assertEquals(OrderStatus.PAID, orders.findByOrderNo("ORD-1").getStatus());
    }

    @Test
    void expiredUntouchedOrderDoesNotAcquireAFalsePaymentBarrier() {
        transactions.executeWithoutResult(status -> {
            TradeOrder order = orders.findLockedByOrderNo("ORD-1");
            ReflectionTestUtils.setField(order, "createdAt", new Date(System.currentTimeMillis() - 16 * 60_000));
        });

        assertThrows(TradeException.class, () -> service(repositories.getRepository(OrderEventRepository.class)).paymentSucceeded("ORD-1", system()));

        assertFalse(orders.findByOrderNo("ORD-1").isPaymentStarted());
        verifyNoInteractions(inventory, coupons);
    }

    @Test
    void exhaustedRecoveryPersistsManualStateWithoutUnlockingOrderOrRetryingEffects() {
        doThrow(new IllegalStateException("coupon unavailable")).when(coupons).consume(anyString(), any(BigDecimal.class));
        doThrow(new IllegalStateException("coupon facts unavailable")).when(coupons).reconcileConsume(anyString(), anyLong(), any(BigDecimal.class));
        OrderService service = service(repositories.getRepository(OrderEventRepository.class));
        assertThrows(IllegalStateException.class, () -> service.paymentSucceeded("ORD-1", system()));
        assertThrows(IllegalStateException.class, () -> service.paymentSucceeded("ORD-1", system()));
        assertThrows(TradeException.class, () -> service.paymentSucceeded("ORD-1", system()));

        TradeOrder pending = orders.findByOrderNo("ORD-1");
        assertEquals("RECONCILIATION_REQUIRED", pending.getPaymentRecoveryStatus());
        assertEquals(2, pending.getPaymentRecoveryAttempts());
        assertTrue(pending.isPaymentStarted());
        assertEquals(OrderStatus.PENDING_PAYMENT, pending.getStatus());
        new OrderPaymentRecoveryScheduler(orders, service).recover();
        verify(inventory, times(1)).commit(eq("ORD-1"), anyList());
        verify(inventory, times(1)).reconcileCommit(eq("ORD-1"), anyList());
    }

    @Test
    void concurrentCancelCannotReleaseInventoryAfterPartiallyExecutedPaymentRollsBack() throws Exception {
        java.util.concurrent.CountDownLatch remoteEntered = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch finishRemote = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch cancelStarted = new java.util.concurrent.CountDownLatch(1);
        doAnswer(call -> {
            remoteEntered.countDown(); assertTrue(finishRemote.await(5, java.util.concurrent.TimeUnit.SECONDS)); return null;
        }).when(inventory).commit(eq("ORD-1"), anyList());
        doThrow(new IllegalStateException("coupon unavailable")).when(coupons).consume(eq("ORD-1"), any(BigDecimal.class));
        OrderService service = service(repositories.getRepository(OrderEventRepository.class));
        java.util.concurrent.ExecutorService workers = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            var payment = workers.submit(() -> service.paymentSucceeded("ORD-1", system()));
            assertTrue(remoteEntered.await(5, java.util.concurrent.TimeUnit.SECONDS));
            var cancel = workers.submit(() -> { cancelStarted.countDown(); return service.cancel(new TradeActor(7L, ActorType.BUYER), "ORD-1"); });
            assertTrue(cancelStarted.await(5, java.util.concurrent.TimeUnit.SECONDS));
            finishRemote.countDown();
            assertInstanceOf(IllegalStateException.class, assertThrows(java.util.concurrent.ExecutionException.class,
                    () -> payment.get(5, java.util.concurrent.TimeUnit.SECONDS)).getCause());
            Throwable rejected = assertThrows(java.util.concurrent.ExecutionException.class,
                    () -> cancel.get(5, java.util.concurrent.TimeUnit.SECONDS)).getCause();
            assertEquals(409, assertInstanceOf(TradeException.class, rejected).getStatus());
            assertEquals(OrderStatus.PENDING_PAYMENT, orders.findByOrderNo("ORD-1").getStatus());
            assertTrue(orders.findByOrderNo("ORD-1").isPaymentStarted());
            verify(inventory, never()).release(anyString());
        } finally { finishRemote.countDown(); workers.shutdownNow(); }
    }

    @Test
    void shipmentWaitingOnPaidCancellationCannotShipAfterRefundRequestCommits() throws Exception {
        TradeSubOrderRepository subOrders = repositories.getRepository(TradeSubOrderRepository.class);
        transactions.executeWithoutResult(status -> {
            orders.findLockedByOrderNo("ORD-1").markPaid();
            var sub = new com.own.order.domain.TradeSubOrder("SUB-1", "ORD-1", 2L, BigDecimal.TEN, BigDecimal.ONE, BigDecimal.ZERO);
            sub.markToShip(); subOrders.save(sub);
        });
        java.util.concurrent.CountDownLatch atCommit = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch releaseCommit = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch shipStarted = new java.util.concurrent.CountDownLatch(1);
        OrderEventRepository realEvents = repositories.getRepository(OrderEventRepository.class);
        OrderEventRepository delayedEvents = mock(OrderEventRepository.class);
        when(delayedEvents.save(any())).thenAnswer(call -> {
            var event = realEvents.save(call.getArgument(0));
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override public void beforeCommit(boolean readOnly) {
                            atCommit.countDown();
                            try { assertTrue(releaseCommit.await(5, java.util.concurrent.TimeUnit.SECONDS)); }
                            catch (InterruptedException error) { Thread.currentThread().interrupt(); throw new IllegalStateException(error); }
                        }
                    });
            return event;
        });
        OrderService service = service(delayedEvents);
        java.util.concurrent.ExecutorService workers = java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            var cancel = workers.submit(() -> service.cancel(new TradeActor(7L, ActorType.BUYER), "ORD-1"));
            assertTrue(atCommit.await(5, java.util.concurrent.TimeUnit.SECONDS));
            var command = new com.own.order.dto.ShipmentCommand(); command.setLogisticsCompany("test"); command.setTrackingNo("TRACK-1");
            var ship = workers.submit(() -> { shipStarted.countDown(); return service.ship(new TradeActor(2L, ActorType.MERCHANT), "SUB-1", command); });
            assertTrue(shipStarted.await(5, java.util.concurrent.TimeUnit.SECONDS));
            releaseCommit.countDown();
            cancel.get(5, java.util.concurrent.TimeUnit.SECONDS);
            Throwable rejected = assertThrows(java.util.concurrent.ExecutionException.class,
                    () -> ship.get(5, java.util.concurrent.TimeUnit.SECONDS)).getCause();
            assertEquals(409, assertInstanceOf(TradeException.class, rejected).getStatus());
            assertEquals(OrderStatus.REFUNDING, orders.findByOrderNo("ORD-1").getStatus());
            assertEquals(com.own.order.domain.SubOrderStatus.TO_SHIP, subOrders.findBySubOrderNo("SUB-1").getStatus());
            assertEquals(0, repositories.getRepository(LogisticsTraceRepository.class).count());
        } finally { releaseCommit.countDown(); workers.shutdownNow(); }
    }

    private OrderService service(OrderEventRepository events) {
        OrderService target = new OrderService(repositories.getRepository(CartItemRepository.class), repositories.getRepository(CartBuyerGuardRepository.class),
                orders, repositories.getRepository(TradeSubOrderRepository.class), repositories.getRepository(OrderItemRepository.class), events,
                mock(CatalogClient.class), inventory, coupons, mock(AddressClient.class), repositories.getRepository(MerchantFreightRuleRepository.class),
                repositories.getRepository(LogisticsTraceRepository.class), repositories.getRepository(AfterSaleRepository.class),
                repositories.getRepository(SensitiveAccessAuditRepository.class));
        ReflectionTestUtils.setField(target, "paymentGuard", guard);
        return proxy(target);
    }
    private TradeActor system() { return new TradeActor(1L, ActorType.SYSTEM); }
    @SuppressWarnings("unchecked")
    private <T> T proxy(T target) {
        ProxyFactory factory = new ProxyFactory(target);
        factory.addAdvice(new TransactionInterceptor(manager, new AnnotationTransactionAttributeSource()));
        return (T) factory.getProxy();
    }
}
