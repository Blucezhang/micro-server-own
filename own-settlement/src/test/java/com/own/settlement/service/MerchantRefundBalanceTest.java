package com.own.settlement.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.settlement.domain.AfterSaleRefund;
import com.own.settlement.domain.MerchantReceivable;
import com.own.settlement.domain.MerchantWithdrawal;
import com.own.settlement.domain.Payment;
import com.own.settlement.domain.SettlementBatch;
import com.own.settlement.repository.AfterSaleRefundRepository;
import com.own.settlement.repository.MerchantReceivableRepository;
import com.own.settlement.repository.MerchantWithdrawalRepository;
import com.own.settlement.repository.SettlementBatchRepository;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MerchantRefundBalanceTest {
    private final MerchantReceivableRepository receivables = mock(MerchantReceivableRepository.class);
    private final MerchantWithdrawalRepository withdrawals = mock(MerchantWithdrawalRepository.class);
    private final AfterSaleRefundRepository refunds = mock(AfterSaleRefundRepository.class);
    private final SettlementBatchRepository batches = mock(SettlementBatchRepository.class);
    private final MerchantSettlementService service = new MerchantSettlementService(receivables, batches, withdrawals, refunds,
            money("0.80"), 7, money("100.00"));
    private final TradeActor merchant = new TradeActor(2L, ActorType.MERCHANT);

    @Test
    void pendingRefundReducesCreditWhenOriginalReceivableIsSettled() {
        MerchantReceivable original = original();
        AfterSaleRefund refund = refund(original, "50.00");
        when(refunds.appliedForReceivable("ORD-1", 2L)).thenReturn(List.of(refund));
        when(receivables.lockSettledByMerchantId(2L)).thenReturn(List.of());
        assertEquals(0, BigDecimal.ZERO.compareTo(service.availableBalance(merchant)));
        when(receivables.findByStatusAndCreatedAtBefore(eq("PENDING"), any(Date.class))).thenReturn(List.of(original));
        when(batches.save(any(SettlementBatch.class))).thenAnswer(call -> call.getArgument(0));
        service.settleDue(new Date());
        when(receivables.lockSettledByMerchantId(2L)).thenReturn(List.of(original));
        assertEquals(money("60.00"), service.availableBalance(merchant));
        assertEquals(money("100.00"), original.getMerchantAmount());
    }

    @Test
    void requestedWithdrawalRemainsReservedAndRefundCreatesNegativeRecoverableBalance() {
        assertNegativeBalance(false);
    }

    @Test
    void paidWithdrawalRemainsDeductedAndRefundCreatesNegativeRecoverableBalance() {
        assertNegativeBalance(true);
    }

    @Test
    void subsequentWholeOrderReversalDoesNotDeductPartialRefundTwice() {
        MerchantReceivable original = original();
        original.settle("STL-1");
        AfterSaleRefund refund = refund(original, "50.00");
        when(refunds.appliedForReceivable("ORD-1", 2L)).thenReturn(List.of(refund));
        when(receivables.findByOrderNo("ORD-1")).thenReturn(List.of(original));
        when(withdrawals.lockReservedByMerchantId(2L)).thenReturn(List.of(withdrawal(true)));
        service.reverseOrder("ORD-1");
        service.reverseOrder("ORD-1");
        when(receivables.lockSettledByMerchantId(2L)).thenReturn(List.of());
        assertEquals(money("-100.00"), service.availableBalance(merchant));
        verify(refunds, never()).appliedForReceivable(anyString(), anyLong());
    }

    @Test
    void newSettledIncomeFirstOffsetsRecoveryBeforeAnotherWithdrawal() {
        MerchantReceivable original = original();
        original.settle("STL-1");
        MerchantReceivable later = new MerchantReceivable("ORD-2", 2L, money("200.00"), money("0.80"));
        later.settle("STL-2");
        when(receivables.lockSettledByMerchantId(2L)).thenReturn(List.of(original, later));
        when(refunds.appliedForReceivable("ORD-1", 2L)).thenReturn(List.of(refund(original, "50.00")));
        when(withdrawals.lockReservedByMerchantId(2L)).thenReturn(List.of(withdrawal(true)));
        when(withdrawals.save(any(MerchantWithdrawal.class))).thenAnswer(call -> call.getArgument(0));
        assertEquals(money("120.00"), service.availableBalance(merchant));
        assertEquals(money("100.00"), service.requestWithdrawal(merchant, money("100.00")).getAmount());
        assertThrows(TradeException.class, () -> service.requestWithdrawal(merchant, money("121.00")));
    }

    @Test
    void allocationUsesHistoricalShareEvenWhenCurrentConfigurationDiffers() {
        MerchantReceivable historical = new MerchantReceivable("ORD-1", 2L, money("125.00"), money("0.37"));
        when(receivables.findByOrderNoAndMerchantId("ORD-1", 2L)).thenReturn(historical);
        AfterSaleRefund refund = newRefund("50.00");
        service.allocateAfterSaleRefund(refund);
        assertEquals(money("18.50"), refund.getMerchantRefundAmount());
        assertEquals(money("31.50"), refund.getPlatformRefundAmount());
    }

    @Test
    void refundAllocationLocksOriginalReceivableAndRequiresAnExistingTransaction() throws Exception {
        assertEquals(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE, MerchantReceivableRepository.class
                .getMethod("findByOrderNoAndMerchantId", String.class, Long.class)
                .getAnnotation(org.springframework.data.jpa.repository.Lock.class).value());
        assertEquals(org.springframework.transaction.annotation.Propagation.MANDATORY, MerchantSettlementService.class
                .getMethod("allocateAfterSaleRefund", AfterSaleRefund.class)
                .getAnnotation(org.springframework.transaction.annotation.Transactional.class).propagation());
    }

    private void assertNegativeBalance(boolean paid) {
        MerchantReceivable original = original();
        original.settle("STL-1");
        when(receivables.lockSettledByMerchantId(2L)).thenReturn(List.of(original));
        when(refunds.appliedForReceivable("ORD-1", 2L)).thenReturn(List.of(refund(original, "50.00")));
        when(withdrawals.lockReservedByMerchantId(2L)).thenReturn(List.of(withdrawal(paid)));
        assertEquals(money("-40.00"), service.availableBalance(merchant));
        TradeException error = assertThrows(TradeException.class, () -> service.requestWithdrawal(merchant, money("100.00")));
        assertEquals(409, error.getStatus());
        verify(withdrawals, never()).save(any(MerchantWithdrawal.class));
    }

    private MerchantWithdrawal withdrawal(boolean paid) {
        MerchantWithdrawal withdrawal = new MerchantWithdrawal("WTH-1", 2L, money("100.00"));
        if (paid) withdrawal.pay();
        return withdrawal;
    }

    private AfterSaleRefund refund(MerchantReceivable original, String amount) {
        AfterSaleRefund refund = newRefund(amount);
        refund.allocate(original, BigDecimal.ZERO, BigDecimal.ZERO);
        return refund;
    }

    private AfterSaleRefund newRefund(String amount) {
        return new AfterSaleRefund("ASR-1", "AS-1", new Payment("PAY-1", "ORD-1", 1L, money("125.00"), "key"), 2L, money(amount));
    }

    private MerchantReceivable original() { return new MerchantReceivable("ORD-1", 2L, money("125.00"), money("0.80")); }
    private BigDecimal money(String value) { return new BigDecimal(value); }
}
