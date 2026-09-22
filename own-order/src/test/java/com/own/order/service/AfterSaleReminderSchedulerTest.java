package com.own.order.service;

import com.own.order.domain.AfterSale;
import com.own.order.domain.AfterSaleStatus;
import com.own.order.domain.AfterSaleType;
import com.own.order.repository.AfterSaleRepository;
import com.own.order.repository.OrderEventRepository;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class AfterSaleReminderSchedulerTest {
    @Test
    public void overdueAuditCreatesOneLocalReminderEvent() {
        AfterSale sale = applyingSale();
        AfterSaleRepository sales = Mockito.mock(AfterSaleRepository.class);
        OrderEventRepository events = Mockito.mock(OrderEventRepository.class);
        Mockito.when(sales.findByStatusAndCreatedAtBefore(Mockito.eq(AfterSaleStatus.APPLYING),
                Mockito.any(java.util.Date.class))).thenReturn(Collections.singletonList(sale));

        new AfterSaleReminderScheduler(sales, events, 48, 24).remindOverdueAudits();

        Assertions.assertNotNull(sale.getLastReminderAt());
        Mockito.verify(sales).save(sale);
        Mockito.verify(events).save(Mockito.any(com.own.order.domain.OrderEvent.class));
    }

    @Test
    public void recentReminderIsNotRepeated() {
        AfterSale sale = applyingSale();
        sale.remind();
        AfterSaleRepository sales = Mockito.mock(AfterSaleRepository.class);
        OrderEventRepository events = Mockito.mock(OrderEventRepository.class);
        Mockito.when(sales.findByStatusAndCreatedAtBefore(Mockito.eq(AfterSaleStatus.APPLYING),
                Mockito.any(java.util.Date.class))).thenReturn(Collections.singletonList(sale));

        new AfterSaleReminderScheduler(sales, events, 48, 24).remindOverdueAudits();

        Mockito.verify(sales, Mockito.never()).save(Mockito.any(AfterSale.class));
        Mockito.verify(events, Mockito.never()).save(Mockito.any(com.own.order.domain.OrderEvent.class));
    }

    private AfterSale applyingSale() {
        AfterSale sale = new AfterSale("AS-1", "ORD-1", "SUB-1", 1L, 2L,
                AfterSaleType.REFUND_ONLY, BigDecimal.ONE, "missing");
        ReflectionTestUtils.setField(sale, "createdAt", new java.util.Date(System.currentTimeMillis() - 72L * 3600L * 1000L));
        return sale;
    }
}
