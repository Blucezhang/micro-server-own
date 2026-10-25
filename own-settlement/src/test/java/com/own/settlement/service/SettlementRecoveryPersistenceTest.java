package com.own.settlement.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.OrderRecoverySnapshot;
import com.own.face.trade.TradeActor;
import com.own.settlement.domain.*;
import com.own.settlement.dto.MerchantSettlementLine;
import com.own.settlement.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
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

class SettlementRecoveryPersistenceTest {
    private EntityManagerFactory entityManagerFactory;
    private JpaTransactionManager manager;
    private TransactionTemplate transactions;
    private PaymentRepository payments;
    private RefundRepository refunds;
    private AfterSaleRefundRepository afterSales;
    private MerchantReceivableRepository receivables;
    private RecoveryOperationRepository operations;
    private MerchantSettlementService merchants;
    private SettlementRecoveryJournal journal;
    private OrderClient orders;

    @BeforeEach
    void openIsolatedDatabase() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource("jdbc:h2:mem:recovery_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.own.settlement.domain");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setJpaPropertyMap(Map.of("hibernate.hbm2ddl.auto", "create-drop"));
        factory.afterPropertiesSet();
        entityManagerFactory = factory.getObject();
        manager = new JpaTransactionManager(entityManagerFactory);
        transactions = new TransactionTemplate(manager);
        EntityManager shared = SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
        JpaRepositoryFactory repositories = new JpaRepositoryFactory(shared);
        payments = repositories.getRepository(PaymentRepository.class);
        refunds = repositories.getRepository(RefundRepository.class);
        afterSales = repositories.getRepository(AfterSaleRefundRepository.class);
        receivables = repositories.getRepository(MerchantReceivableRepository.class);
        operations = repositories.getRepository(RecoveryOperationRepository.class);
        merchants = proxy(new MerchantSettlementService(receivables, repositories.getRepository(SettlementBatchRepository.class),
                repositories.getRepository(MerchantWithdrawalRepository.class), afterSales, new BigDecimal("0.80"), 7, new BigDecimal("100.00")));
        journal = proxy(new SettlementRecoveryJournal(operations, new com.own.face.trade.config.TradeRecoveryProperties(3, 1000, 30000)));
        orders = mock(OrderClient.class);
        when(orders.settlementLines("ORD-1")).thenReturn(List.of(new MerchantSettlementLine(2L, BigDecimal.TEN)));
        transactions.executeWithoutResult(status -> payments.save(new Payment("PAY-1", "ORD-1", 7L, BigDecimal.TEN, "key")));
    }

    @AfterEach
    void closeIsolatedDatabase() { if (entityManagerFactory != null) entityManagerFactory.close(); }

    @Test
    void lostOrderResponseLeavesDurableIntentAndFreshExecutorReconcilesWithoutRepeatingCallback() {
        AtomicBoolean remotePaid = new AtomicBoolean();
        doAnswer(call -> {
            remotePaid.set(true);
            throw new IllegalStateException("injected response loss after order commit");
        }).when(orders).paymentSucceeded("ORD-1");
        when(orders.recoverySnapshot("ORD-1")).thenAnswer(call -> snapshot(remotePaid.get() ? "PAID" : "PENDING_PAYMENT"));

        assertThrows(IllegalStateException.class, () -> service(merchants).simulateSuccess(system(), "PAY-1"));

        assertEquals(PaymentStatus.CREATED, payments.findByPaymentNo("PAY-1").getStatus());
        assertEquals(0, receivables.count());
        assertEquals("PENDING", operation(RecoveryAction.PAYMENT).getStatus());
        assertEquals(1, operation(RecoveryAction.PAYMENT).getAttempts());

        SettlementService restarted = service(merchants);
        restarted.recover("PAY-1", RecoveryAction.PAYMENT);
        restarted.recover("PAY-1", RecoveryAction.PAYMENT);

        assertEquals(PaymentStatus.SUCCEEDED, payments.findByPaymentNo("PAY-1").getStatus());
        assertEquals(1, receivables.count());
        assertEquals("COMPLETED", operation(RecoveryAction.PAYMENT).getStatus());
        verify(orders, times(1)).paymentSucceeded("ORD-1");
        Boolean unresolved = transactions.execute(status -> journal.hasUnresolvedPayment("PAY-1"));
        assertEquals(Boolean.FALSE, unresolved);
    }

    @Test
    void failureAfterLocalReceivableInsertRollsItBackButPreservesIndependentIntent() {
        MerchantSettlementService failing = mock(MerchantSettlementService.class);
        doAnswer(call -> {
            merchants.recordPaidOrder(call.getArgument(0), call.getArgument(1));
            throw new IllegalStateException("injected failure after receivable insert");
        }).when(failing).recordPaidOrder(eq("ORD-1"), anyList());
        when(orders.recoverySnapshot("ORD-1")).thenReturn(snapshot("PAID"));

        assertThrows(IllegalStateException.class, () -> service(failing).simulateSuccess(system(), "PAY-1"));

        assertEquals(0, receivables.count());
        assertEquals(PaymentStatus.CREATED, payments.findByPaymentNo("PAY-1").getStatus());
        assertEquals("PENDING", operation(RecoveryAction.PAYMENT).getStatus());
        service(merchants).recover("PAY-1", RecoveryAction.PAYMENT);
        assertEquals(1, receivables.count());
        assertEquals("COMPLETED", operation(RecoveryAction.PAYMENT).getStatus());
        verify(orders, times(1)).paymentSucceeded("ORD-1");
    }

    @Test
    void refundResponseLossReconcilesOriginalReceivableExactlyOnce() {
        SettlementService initial = service(merchants);
        initial.simulateSuccess(system(), "PAY-1");
        doThrow(new IllegalStateException("injected refund response loss")).when(orders).refundSucceeded("ORD-1");
        when(orders.recoverySnapshot("ORD-1")).thenReturn(snapshot("REFUNDED"));

        assertThrows(IllegalStateException.class, () -> initial.refund(new TradeActor(7L, ActorType.BUYER), "ORD-1"));

        assertEquals(PaymentStatus.SUCCEEDED, payments.findByPaymentNo("PAY-1").getStatus());
        assertEquals(0, refunds.count());
        assertEquals("PENDING", operation(RecoveryAction.REFUND).getStatus());
        SettlementService restarted = service(merchants);
        restarted.recover("PAY-1", RecoveryAction.REFUND);
        restarted.recover("PAY-1", RecoveryAction.REFUND);

        assertEquals(PaymentStatus.REFUNDED, payments.findByPaymentNo("PAY-1").getStatus());
        assertEquals(1, refunds.count());
        assertEquals("REVERSED", transactions.execute(status -> receivables.findByOrderNoAndMerchantId("ORD-1", 2L).getStatus()));
        assertEquals("COMPLETED", operation(RecoveryAction.REFUND).getStatus());
        verify(orders, times(1)).refundSucceeded("ORD-1");
    }

    @Test
    void unresolvedSuccessCannotBeOverwrittenBySimulatedFailureAndAttemptsBecomeManual() {
        doThrow(new IllegalStateException("order unavailable")).when(orders).paymentSucceeded("ORD-1");
        when(orders.recoverySnapshot("ORD-1")).thenThrow(new IllegalStateException("facts unavailable"));
        SettlementService service = service(merchants);
        assertThrows(IllegalStateException.class, () -> service.simulateSuccess(system(), "PAY-1"));
        assertThrows(com.own.face.trade.TradeException.class, () -> service.simulateFailure(system(), "PAY-1"));
        for (int attempt = 0; attempt < 2; attempt++) assertThrows(IllegalStateException.class, () -> service.recover("PAY-1", RecoveryAction.PAYMENT));
        assertThrows(com.own.face.trade.TradeException.class, () -> service.recover("PAY-1", RecoveryAction.PAYMENT));
        assertEquals("RECONCILIATION_REQUIRED", operation(RecoveryAction.PAYMENT).getStatus());
        assertEquals(3, operation(RecoveryAction.PAYMENT).getAttempts());
        assertEquals(PaymentStatus.CREATED, payments.findByPaymentNo("PAY-1").getStatus());
        assertEquals(0, receivables.count());
    }

    @Test
    void mismatchedAuthoritativeSnapshotCannotCreditMerchant() {
        doThrow(new IllegalStateException("response lost")).when(orders).paymentSucceeded("ORD-1");
        SettlementService service = service(merchants);
        assertThrows(IllegalStateException.class, () -> service.simulateSuccess(system(), "PAY-1"));
        when(orders.recoverySnapshot("ORD-1")).thenReturn(new OrderRecoverySnapshot("ORD-1", 99L, BigDecimal.TEN, "PAID", "COMPLETED"));
        assertThrows(com.own.face.trade.TradeException.class, () -> service.recover("PAY-1", RecoveryAction.PAYMENT));
        assertEquals(0, receivables.count());
        assertEquals("PENDING", operation(RecoveryAction.PAYMENT).getStatus());
    }

    @Test
    void paymentCommitFailureRollsBackCompletedMarkerTogetherWithAllLocalFinancialWrites() {
        doAnswer(call -> { failOuterCommit(); return null; }).when(orders).paymentSucceeded("ORD-1");
        when(orders.recoverySnapshot("ORD-1")).thenReturn(snapshot("PAID"));
        assertThrows(IllegalStateException.class, () -> service(merchants).simulateSuccess(system(), "PAY-1"));
        assertEquals(0, receivables.count());
        assertEquals(PaymentStatus.CREATED, payments.findByPaymentNo("PAY-1").getStatus());
        assertEquals("PENDING", operation(RecoveryAction.PAYMENT).getStatus());

        service(merchants).recover("PAY-1", RecoveryAction.PAYMENT);

        assertEquals(1, receivables.count());
        assertEquals(PaymentStatus.SUCCEEDED, payments.findByPaymentNo("PAY-1").getStatus());
        assertEquals("COMPLETED", operation(RecoveryAction.PAYMENT).getStatus());
        verify(orders, times(1)).paymentSucceeded("ORD-1");
    }

    @Test
    void refundCommitFailureRollsBackReversalRefundAndCompletionButNotIntent() {
        service(merchants).simulateSuccess(system(), "PAY-1");
        doAnswer(call -> { failOuterCommit(); return null; }).when(orders).refundSucceeded("ORD-1");
        when(orders.recoverySnapshot("ORD-1")).thenReturn(snapshot("REFUNDED"));
        assertThrows(IllegalStateException.class, () -> service(merchants).refund(new TradeActor(7L, ActorType.BUYER), "ORD-1"));
        assertEquals(0, refunds.count());
        assertEquals(PaymentStatus.SUCCEEDED, payments.findByPaymentNo("PAY-1").getStatus());
        assertEquals("PENDING", transactions.execute(status -> receivables.findByOrderNoAndMerchantId("ORD-1", 2L).getStatus()));
        assertEquals("PENDING", operation(RecoveryAction.REFUND).getStatus());

        service(merchants).recover("PAY-1", RecoveryAction.REFUND);

        assertEquals(1, refunds.count());
        assertEquals("REVERSED", transactions.execute(status -> receivables.findByOrderNoAndMerchantId("ORD-1", 2L).getStatus()));
        assertEquals("COMPLETED", operation(RecoveryAction.REFUND).getStatus());
        verify(orders, times(1)).refundSucceeded("ORD-1");
    }

    private void failOuterCommit() {
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override public void beforeCommit(boolean readOnly) { throw new IllegalStateException("injected before-commit failure"); }
                });
    }

    @Test
    void projectionReadOutageCannotLoseTheAlreadyConfirmedProviderSuccessIntent() {
        when(orders.settlementLines("ORD-1")).thenThrow(new IllegalStateException("projection unavailable"))
                .thenReturn(List.of(new MerchantSettlementLine(2L, BigDecimal.TEN)));
        when(orders.recoverySnapshot("ORD-1")).thenReturn(new OrderRecoverySnapshot("ORD-1", 7L, BigDecimal.TEN, "PENDING_PAYMENT", "NONE"));

        assertThrows(IllegalStateException.class, () -> service(merchants).simulateSuccess(system(), "PAY-1"));

        assertEquals("PENDING", operation(RecoveryAction.PAYMENT).getStatus());
        assertEquals(0, receivables.count());
        verify(orders, never()).paymentSucceeded("ORD-1");
        service(merchants).recover("PAY-1", RecoveryAction.PAYMENT);
        assertEquals(PaymentStatus.SUCCEEDED, payments.findByPaymentNo("PAY-1").getStatus());
        assertEquals("COMPLETED", operation(RecoveryAction.PAYMENT).getStatus());
        verify(orders, times(1)).paymentSucceeded("ORD-1");
    }

    private RecoveryOperation operation(RecoveryAction action) {
        return transactions.execute(status -> operations.findByOperationKey(RecoveryOperation.key("PAY-1", action)));
    }
    private OrderRecoverySnapshot snapshot(String status) {
        return new OrderRecoverySnapshot("ORD-1", 7L, BigDecimal.TEN, status, "COMPLETED");
    }
    private TradeActor system() { return new TradeActor(1L, ActorType.SYSTEM); }
    private SettlementService service(MerchantSettlementService ledger) {
        SettlementService target = new SettlementService(payments, orders, refunds, afterSales);
        ReflectionTestUtils.setField(target, "merchantSettlementService", ledger);
        ReflectionTestUtils.setField(target, "recoveryJournal", journal);
        return proxy(target);
    }
    @SuppressWarnings("unchecked")
    private <T> T proxy(T target) {
        ProxyFactory factory = new ProxyFactory(target);
        factory.addAdvice(new TransactionInterceptor(manager, new AnnotationTransactionAttributeSource()));
        return (T) factory.getProxy();
    }
}
