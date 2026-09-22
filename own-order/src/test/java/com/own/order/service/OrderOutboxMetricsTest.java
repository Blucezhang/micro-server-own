package com.own.order.service;

import com.own.order.repository.OrderEventRepository;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.actuate.metrics.Metric;

public class OrderOutboxMetricsTest {
    @Test
    public void exposesPendingAndFailedOutboxGauges() {
        OrderEventRepository repository = Mockito.mock(OrderEventRepository.class);
        Mockito.when(repository.countByDeliveryStatus("PENDING")).thenReturn(3L);
        Mockito.when(repository.countByDeliveryStatus("FAILED")).thenReturn(2L);

        Collection<Metric<?>> metrics = new OrderOutboxMetrics(repository).metrics();

        Assert.assertEquals(2, metrics.size());
        for (Metric<?> metric : metrics) {
            if ("trade.outbox.pending".equals(metric.getName())) Assert.assertEquals(3L, metric.getValue());
            if ("trade.outbox.failed".equals(metric.getName())) Assert.assertEquals(2L, metric.getValue());
        }
    }
}
