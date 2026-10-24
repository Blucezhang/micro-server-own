package com.own.settlement.domain;

import com.own.face.trade.TradeException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AfterSaleRefundAllocationTest {
    @Test
    void cumulativeRoundingReturnsEveryOriginalCentExactlyOnce() {
        MerchantReceivable receivable = new MerchantReceivable("ORD-1", 2L, money("0.03"), money("0.6667"));
        BigDecimal gross = BigDecimal.ZERO;
        BigDecimal merchant = BigDecimal.ZERO;
        BigDecimal platform = BigDecimal.ZERO;
        String[] expectedMerchant = {"0.01", "0.00", "0.01"};
        for (int index = 0; index < 3; index++) {
            AfterSaleRefund refund = refund("0.01");
            refund.allocate(receivable, gross, merchant);
            assertEquals(money(expectedMerchant[index]), refund.getMerchantRefundAmount());
            assertEquals(refund.getAmount(), refund.getMerchantRefundAmount().add(refund.getPlatformRefundAmount()));
            gross = gross.add(refund.getAmount());
            merchant = merchant.add(refund.getMerchantRefundAmount());
            platform = platform.add(refund.getPlatformRefundAmount());
        }
        assertEquals(receivable.getMerchantAmount(), merchant);
        assertEquals(receivable.getPlatformAmount(), platform);
        assertEquals(money("0.03"), receivable.getGrossAmount());
    }

    @Test
    void usesStoredRoundedShareRatherThanNominalOrCurrentRate() {
        MerchantReceivable receivable = new MerchantReceivable("ORD-1", 2L, money("0.03"), money("0.50"));
        AfterSaleRefund refund = refund("0.02");
        refund.allocate(receivable, BigDecimal.ZERO, BigDecimal.ZERO);
        assertEquals(money("0.01"), refund.getMerchantRefundAmount());
        assertEquals(money("0.01"), refund.getPlatformRefundAmount());
        assertEquals("APPLIED", refund.getReconciliationStatus());
    }

    @Test
    void cumulativeGrossCannotExceedThatMerchantsOriginalReceivable() {
        AfterSaleRefund refund = refund("0.02");
        TradeException error = assertThrows(TradeException.class,
                () -> refund.allocate(receivable(), money("0.02"), money("0.01")));
        assertEquals(409, error.getStatus());
        assertNull(refund.getMerchantRefundAmount());
    }

    @Test
    void rejectsDifferentOrderAndDifferentMerchant() {
        for (MerchantReceivable receivable : new MerchantReceivable[] {
                new MerchantReceivable("ORD-2", 2L, money("0.03"), money("0.80")),
                new MerchantReceivable("ORD-1", 3L, money("0.03"), money("0.80"))}) {
            assertThrows(TradeException.class, () -> refund("0.01").allocate(receivable, BigDecimal.ZERO, BigDecimal.ZERO));
        }
    }

    @Test
    void rejectsMissingOrReversedReceivable() {
        assertThrows(TradeException.class, () -> refund("0.01").allocate(null, BigDecimal.ZERO, BigDecimal.ZERO));
        MerchantReceivable reversed = receivable();
        reversed.reverse();
        assertThrows(TradeException.class, () -> refund("0.01").allocate(reversed, BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Test
    void allocationCannotBeAppliedTwice() {
        AfterSaleRefund refund = refund("0.01");
        refund.allocate(receivable(), BigDecimal.ZERO, BigDecimal.ZERO);
        assertThrows(TradeException.class, () -> refund.allocate(receivable(), BigDecimal.ZERO, BigDecimal.ZERO));
        assertEquals(money("0.01"), refund.getMerchantRefundAmount());
    }

    @Test
    void inconsistentPriorSplitIsNotSilentlyOverwritten() {
        AfterSaleRefund refund = refund("0.01");
        assertThrows(TradeException.class, () -> refund.allocate(receivable(), money("0.01"), money("0.00")));
        assertNull(refund.getMerchantRefundAmount());
    }

    @Test
    void zeroAndOneHundredPercentSharesRemainExact() {
        for (String rate : new String[] {"0.00", "1.00"}) {
            MerchantReceivable receivable = new MerchantReceivable("ORD-1", 2L, money("0.03"), money(rate));
            AfterSaleRefund refund = refund("0.03");
            refund.allocate(receivable, BigDecimal.ZERO, BigDecimal.ZERO);
            assertEquals(receivable.getMerchantAmount(), refund.getMerchantRefundAmount());
            assertEquals(receivable.getPlatformAmount(), refund.getPlatformRefundAmount());
        }
    }

    @Test
    void persistenceGuardRejectsNewUnallocatedRefundButAcceptsAppliedAllocation() throws Exception {
        var guard = AfterSaleRefund.class.getDeclaredMethod("requireAllocatedNewRefund");
        assertNotNull(guard.getAnnotation(jakarta.persistence.PrePersist.class));
        guard.setAccessible(true);
        AfterSaleRefund refund = refund("0.01");
        java.lang.reflect.InvocationTargetException error = assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> guard.invoke(refund));
        assertInstanceOf(IllegalStateException.class, error.getCause());
        refund.allocate(receivable(), BigDecimal.ZERO, BigDecimal.ZERO);
        assertDoesNotThrow(() -> guard.invoke(refund));
    }

    private MerchantReceivable receivable() {
        return new MerchantReceivable("ORD-1", 2L, money("0.03"), money("0.50"));
    }

    private AfterSaleRefund refund(String amount) {
        return new AfterSaleRefund("ASR-1", "AS-1", new Payment("PAY-1", "ORD-1", 1L, money("10.00"), "key"), 2L, money(amount));
    }

    private BigDecimal money(String value) { return new BigDecimal(value); }
}
