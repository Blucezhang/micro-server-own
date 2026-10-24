package com.own.settlement.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.settlement.domain.AfterSaleRefund;
import com.own.settlement.domain.MerchantReceivable;
import com.own.settlement.domain.Payment;
import com.own.settlement.dto.AfterSaleRefundCommand;
import com.own.settlement.repository.AfterSaleRefundRepository;
import com.own.settlement.repository.MerchantReceivableRepository;
import com.own.settlement.repository.MerchantWithdrawalRepository;
import com.own.settlement.repository.PaymentRepository;
import com.own.settlement.repository.RefundRepository;
import com.own.settlement.repository.SettlementBatchRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AfterSaleMerchantRefundTest {
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final AfterSaleRefundRepository refunds = mock(AfterSaleRefundRepository.class);
    private final MerchantReceivableRepository receivables = mock(MerchantReceivableRepository.class);
    private final OrderClient orders = mock(OrderClient.class);
    private final RecordingTransactionManager transactions = new RecordingTransactionManager();
    private final MerchantSettlementService merchants = new MerchantSettlementService(receivables,
            mock(SettlementBatchRepository.class), mock(MerchantWithdrawalRepository.class), refunds,
            money("0.80"), 7, money("100.00"));
    private final SettlementService service = service();

    @Test
    void multiMerchantOrderOnlyAdjustsTheAttributedHistoricalReceivable() {
        Payment payment = paid("300.00");
        MerchantReceivable first = new MerchantReceivable("ORD-1", 2L, money("100.00"), money("0.37"));
        MerchantReceivable other = new MerchantReceivable("ORD-1", 3L, money("200.00"), money("0.80"));
        when(receivables.findByOrderNoAndMerchantId("ORD-1", 2L)).thenReturn(first);
        saveReturnsRefund();

        AfterSaleRefund result = execute(command("AS-1", 2L, "40.00"));

        assertEquals(payment.getPaymentNo(), result.getPaymentNo());
        assertEquals(2L, result.getMerchantId());
        assertEquals(money("14.80"), result.getMerchantRefundAmount());
        assertEquals(money("25.20"), result.getPlatformRefundAmount());
        assertEquals("APPLIED", result.getReconciliationStatus());
        assertEquals(money("37.00"), first.getMerchantAmount());
        assertEquals(money("160.00"), other.getMerchantAmount());
        verify(receivables, never()).findByOrderNoAndMerchantId("ORD-1", 3L);
        verify(receivables, never()).save(any(MerchantReceivable.class));
        var sequence = inOrder(payments, refunds, receivables, orders);
        sequence.verify(payments).findByOrderNoForUpdate("ORD-1");
        sequence.verify(refunds).findByAfterSaleNo("AS-1");
        sequence.verify(refunds).succeededAmountByPaymentNo("PAY-1");
        sequence.verify(receivables).findByOrderNoAndMerchantId("ORD-1", 2L);
        sequence.verify(refunds).appliedForReceivable("ORD-1", 2L);
        sequence.verify(refunds).save(result);
        sequence.verify(orders).afterSaleRefundSucceeded("AS-1");
    }

    @Test
    void repeatedPartialRefundsAndReplayDoNotLoseOrDoubleDeductRoundingCents() {
        paid("0.03");
        when(receivables.findByOrderNoAndMerchantId("ORD-1", 2L))
                .thenReturn(new MerchantReceivable("ORD-1", 2L, money("0.03"), money("0.50")));
        List<AfterSaleRefund> stored = new ArrayList<>();
        when(refunds.appliedForReceivable("ORD-1", 2L)).thenAnswer(call -> List.copyOf(stored));
        when(refunds.succeededAmountByPaymentNo("PAY-1"))
                .thenAnswer(call -> stored.stream().map(AfterSaleRefund::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        when(refunds.findByAfterSaleNo(anyString())).thenAnswer(call -> stored.stream()
                .filter(refund -> refund.getAfterSaleNo().equals(call.getArgument(0))).findFirst().orElse(null));
        when(refunds.save(any(AfterSaleRefund.class))).thenAnswer(call -> {
            AfterSaleRefund refund = call.getArgument(0);
            stored.add(refund);
            return refund;
        });

        execute(command("AS-1", 2L, "0.01"));
        execute(command("AS-2", 2L, "0.01"));
        execute(command("AS-3", 2L, "0.01"));
        assertSame(stored.get(1), execute(command("AS-2", 2L, "0.01")));

        assertEquals(3, stored.size());
        assertEquals(money("0.02"), stored.stream().map(AfterSaleRefund::getMerchantRefundAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        assertEquals(money("0.01"), stored.stream().map(AfterSaleRefund::getPlatformRefundAmount).reduce(BigDecimal.ZERO, BigDecimal::add));
        verify(refunds, times(3)).save(any(AfterSaleRefund.class));
        verify(receivables, times(3)).findByOrderNoAndMerchantId("ORD-1", 2L);
    }

    @Test
    void sameAfterSaleCannotBeReplayedWithAnotherMerchantOrAmount() {
        Payment payment = paid("100.00");
        AfterSaleRefund existing = new AfterSaleRefund("ASR-1", "AS-1", payment, 2L, money("40.00"));
        when(refunds.findByAfterSaleNo("AS-1")).thenReturn(existing);
        assertEquals(409, assertThrows(TradeException.class, () -> execute(command("AS-1", 3L, "40.00"))).getStatus());
        assertEquals(409, assertThrows(TradeException.class, () -> execute(command("AS-1", 2L, "41.00"))).getStatus());
        verify(refunds, never()).save(any(AfterSaleRefund.class));
        verifyNoInteractions(orders, receivables);
    }

    @Test
    void obsoleteUnattributedWriteContractIsRejectedBeforeAnyDatabaseRead() {
        for (Long merchant : new Long[] {null, 0L, -1L}) {
            assertEquals(422, assertThrows(TradeException.class,
                    () -> execute(command("AS-1", merchant, "40.00"))).getStatus());
        }
        verifyNoInteractions(payments, refunds, receivables, orders);
    }

    @Test
    void missingReceivableCannotProduceAnUnadjustedSuccessfulRefund() {
        paid("100.00");
        assertEquals(409, assertThrows(TradeException.class,
                () -> execute(command("AS-1", 2L, "40.00"))).getStatus());
        verify(refunds, never()).save(any(AfterSaleRefund.class));
        verifyNoInteractions(orders);
        assertEquals(1, transactions.rollbacks);
    }

    @Test
    void merchantCapCannotBeBypassedByLargerMultiMerchantPayment() {
        paid("300.00");
        when(receivables.findByOrderNoAndMerchantId("ORD-1", 2L))
                .thenReturn(new MerchantReceivable("ORD-1", 2L, money("30.00"), money("0.80")));
        assertEquals(409, assertThrows(TradeException.class,
                () -> execute(command("AS-1", 2L, "40.00"))).getStatus());
        verify(refunds, never()).save(any(AfterSaleRefund.class));
        verifyNoInteractions(orders);
    }

    @Test
    void legacyUnknownMerchantStaysPendingEvenOnAnAttributedRetry() {
        Payment payment = paid("100.00");
        AfterSaleRefund legacy = new AfterSaleRefund("ASR-1", "AS-1", payment, 2L, money("40.00"));
        ReflectionTestUtils.setField(legacy, "merchantId", null);
        when(refunds.findByAfterSaleNo("AS-1")).thenReturn(legacy);

        assertSame(legacy, execute(command("AS-1", 2L, "40.00")));

        assertEquals("PENDING_RECONCILIATION", legacy.getReconciliationStatus());
        assertNull(legacy.getMerchantId());
        assertNull(legacy.getMerchantRefundAmount());
        assertNull(legacy.getPlatformRefundAmount());
        verify(refunds, never()).save(any(AfterSaleRefund.class));
        verifyNoInteractions(receivables);
        verify(orders).afterSaleRefundSucceeded("AS-1");
    }

    @Test
    void failedRefundSaveRollsBackBeforeNotificationAndRetryCanAllocateOnce() {
        paid("100.00");
        MerchantReceivable original = new MerchantReceivable("ORD-1", 2L, money("100.00"), money("0.80"));
        when(receivables.findByOrderNoAndMerchantId("ORD-1", 2L)).thenReturn(original);
        when(refunds.save(any(AfterSaleRefund.class)))
                .thenThrow(new IllegalStateException("injected refund insert failure"))
                .thenAnswer(call -> call.getArgument(0));

        assertThrows(IllegalStateException.class, () -> execute(command("AS-1", 2L, "40.00")));
        assertEquals(1, transactions.rollbacks);
        assertEquals(0, transactions.commits);
        assertEquals(money("80.00"), original.getMerchantAmount());
        verifyNoInteractions(orders);

        AfterSaleRefund result = execute(command("AS-1", 2L, "40.00"));
        assertEquals(money("32.00"), result.getMerchantRefundAmount());
        assertEquals(1, transactions.commits);
        verify(orders, times(1)).afterSaleRefundSucceeded("AS-1");
    }

    @Test
    void failedTransactionCommitDoesNotSendRefundSuccessNotification() {
        paid("100.00");
        MerchantReceivable original = new MerchantReceivable("ORD-1", 2L, money("100.00"), money("0.80"));
        when(receivables.findByOrderNoAndMerchantId("ORD-1", 2L)).thenReturn(original);
        saveReturnsRefund();
        transactions.failCommit = true;

        assertThrows(IllegalStateException.class, () -> execute(command("AS-1", 2L, "40.00")));

        assertEquals(0, transactions.commits);
        assertEquals(money("80.00"), original.getMerchantAmount());
        verifyNoInteractions(orders);
        // A commit failure is not proof of database rollback; no independent receivable write exists.
        verify(receivables, never()).save(any(MerchantReceivable.class));
    }

    @Test
    void pendingHistoricalRefundsStillConsumeThePaymentRefundLimit() {
        paid("100.00");
        when(refunds.succeededAmountByPaymentNo("PAY-1")).thenReturn(money("80.00"));

        assertEquals(409, assertThrows(TradeException.class,
                () -> execute(command("AS-NEW", 2L, "40.00"))).getStatus());

        verifyNoInteractions(receivables, orders);
        verify(refunds, never()).save(any(AfterSaleRefund.class));
    }

    @Test
    void replayCannotRebindAnExistingRefundToAnotherPaymentForTheSameOrder() {
        paid("100.00");
        Payment other = new Payment("PAY-OTHER", "ORD-1", 1L, money("100.00"), "other-key");
        when(refunds.findByAfterSaleNo("AS-1"))
                .thenReturn(new AfterSaleRefund("ASR-1", "AS-1", other, 2L, money("40.00")));

        assertEquals(409, assertThrows(TradeException.class,
                () -> execute(command("AS-1", 2L, "40.00"))).getStatus());

        verifyNoInteractions(receivables, orders);
        verify(refunds, never()).save(any(AfterSaleRefund.class));
    }

    private Payment paid(String amount) {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, money(amount), "key");
        payment.succeed();
        when(payments.findByOrderNoForUpdate("ORD-1")).thenReturn(payment);
        return payment;
    }

    private void saveReturnsRefund() {
        when(refunds.save(any(AfterSaleRefund.class))).thenAnswer(call -> call.getArgument(0));
    }

    private SettlementService service() {
        SettlementService target = new SettlementService(payments, orders, mock(RefundRepository.class), refunds);
        ReflectionTestUtils.setField(target, "merchantSettlementService", merchants);
        ProxyFactory proxy = new ProxyFactory(target);
        proxy.addAdvice(new TransactionInterceptor(transactions, new AnnotationTransactionAttributeSource()));
        return (SettlementService) proxy.getProxy();
    }

    private AfterSaleRefund execute(AfterSaleRefundCommand command) {
        return service.refundAfterSale(new TradeActor(1L, ActorType.SYSTEM), command);
    }

    private AfterSaleRefundCommand command(String no, Long merchantId, String amount) {
        AfterSaleRefundCommand command = new AfterSaleRefundCommand();
        command.setAfterSaleNo(no); command.setOrderNo("ORD-1"); command.setBuyerId(1L);
        command.setMerchantId(merchantId); command.setAmount(money(amount));
        return command;
    }

    private BigDecimal money(String value) { return new BigDecimal(value); }

    private static class RecordingTransactionManager extends AbstractPlatformTransactionManager {
        int commits;
        int rollbacks;
        boolean failCommit;
        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) { }
        @Override protected void doCommit(DefaultTransactionStatus status) {
            if (failCommit) throw new IllegalStateException("injected transaction commit failure");
            commits++;
        }
        @Override protected void doRollback(DefaultTransactionStatus status) { rollbacks++; }
    }
}
