package com.own.order.service;

import com.own.order.domain.AfterSale;
import com.own.order.domain.AfterSaleStatus;
import com.own.order.domain.AfterSaleType;
import com.own.order.repository.AfterSaleRepository;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.Test;
import org.mockito.Mockito;

public class AfterSaleRefundRetrySchedulerTest {
    @Test
    public void pendingRefundIsRetriedAndATransientFailureDoesNotStopOtherWork() {
        AfterSaleRepository sales = Mockito.mock(AfterSaleRepository.class);
        SettlementClient settlement = Mockito.mock(SettlementClient.class);
        AfterSale sale = new AfterSale("AS-1", "ORD-1", "SUB-1", 1L, 2L,
                AfterSaleType.REFUND_ONLY, BigDecimal.ONE, "missing");
        sale.approve("approved");
        Mockito.when(sales.findByStatus(AfterSaleStatus.REFUND_PENDING)).thenReturn(Collections.singletonList(sale));
        Mockito.doThrow(new IllegalStateException("temporary outage")).when(settlement).refundAfterSale(sale);

        new AfterSaleRefundRetryScheduler(sales, settlement).retryPendingRefunds();

        Mockito.verify(settlement).refundAfterSale(sale);
    }
}
