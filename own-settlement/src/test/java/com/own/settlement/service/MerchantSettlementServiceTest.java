package com.own.settlement.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.settlement.domain.MerchantReceivable;
import com.own.settlement.dto.MerchantSettlementLine;
import com.own.settlement.repository.MerchantReceivableRepository;
import com.own.settlement.repository.MerchantWithdrawalRepository;
import com.own.settlement.repository.SettlementBatchRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class MerchantSettlementServiceTest {
    @Test
    public void paidOrderCreatesEightyTwentyReceivable() {
        MerchantReceivableRepository receivables = mock(MerchantReceivableRepository.class);
        when(receivables.save(any(MerchantReceivable.class))).thenAnswer(i -> i.getArguments()[0]);
        MerchantSettlementService service = service(receivables, mock(MerchantWithdrawalRepository.class));
        MerchantReceivable receivable = service.recordPaidOrder("ORD-1", Arrays.asList(new MerchantSettlementLine(2L, new BigDecimal("125.00")))).get(0);
        assertEquals(new BigDecimal("100.00"), receivable.getMerchantAmount());
        assertEquals(new BigDecimal("25.00"), receivable.getPlatformAmount());
    }

    @Test
    public void withdrawalAtConfiguredMinimumIsAllowed() {
        MerchantReceivable settled = new MerchantReceivable("ORD-1", 2L, new BigDecimal("125.00"), new BigDecimal("0.80"));
        settled.settle("STL-1");
        MerchantReceivableRepository receivables = mock(MerchantReceivableRepository.class);
        MerchantWithdrawalRepository withdrawals = mock(MerchantWithdrawalRepository.class);
        when(receivables.lockSettledByMerchantId(2L)).thenReturn(Arrays.asList(settled));
        when(withdrawals.lockReservedByMerchantId(2L)).thenReturn(Collections.emptyList());
        when(withdrawals.save(any(com.own.settlement.domain.MerchantWithdrawal.class))).thenAnswer(i -> i.getArguments()[0]);
        assertTrue(service(receivables, withdrawals).requestWithdrawal(new TradeActor(2L, ActorType.MERCHANT), new BigDecimal("100.00")).getWithdrawalNo().startsWith("WTH-"));
    }

    @Test
    public void withdrawalReservesSettledBalance() {
        MerchantReceivable settled = new MerchantReceivable("ORD-1", 2L, new BigDecimal("200.00"), new BigDecimal("0.80"));
        settled.settle("STL-1");
        MerchantReceivableRepository receivables = mock(MerchantReceivableRepository.class);
        MerchantWithdrawalRepository withdrawals = mock(MerchantWithdrawalRepository.class);
        when(receivables.lockSettledByMerchantId(2L)).thenReturn(Arrays.asList(settled));
        when(withdrawals.lockReservedByMerchantId(2L)).thenReturn(Collections.emptyList());
        when(withdrawals.save(any(com.own.settlement.domain.MerchantWithdrawal.class))).thenAnswer(i -> i.getArguments()[0]);
        assertTrue(service(receivables, withdrawals).requestWithdrawal(new TradeActor(2L, ActorType.MERCHANT), new BigDecimal("150.00")).getWithdrawalNo().startsWith("WTH-"));
    }

    @Test
    public void refundCanReverseASettledReceivable() {
        MerchantReceivable settled = new MerchantReceivable("ORD-1", 2L, new BigDecimal("200.00"), new BigDecimal("0.80"));
        settled.settle("STL-1");
        MerchantReceivableRepository receivables = mock(MerchantReceivableRepository.class);
        when(receivables.findByOrderNo("ORD-1")).thenReturn(Arrays.asList(settled));
        service(receivables, mock(MerchantWithdrawalRepository.class)).reverseOrder("ORD-1");
        assertEquals("REVERSED", settled.getStatus());
        verify(receivables).findByOrderNo("ORD-1");
    }

    private MerchantSettlementService service(MerchantReceivableRepository receivables, MerchantWithdrawalRepository withdrawals) {
        return new MerchantSettlementService(receivables, mock(SettlementBatchRepository.class), withdrawals,
                new BigDecimal("0.80"), 7, new BigDecimal("100.00"));
    }
}
