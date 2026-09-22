package com.own.order.service;

import com.own.order.repository.OrderEventRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class OrderOutboxMetricsTest {
    @Test
    public void exposesPendingAndFailedOutboxGauges() {
        OrderEventRepository repository = Mockito.mock(OrderEventRepository.class);
        Mockito.when(repository.countByDeliveryStatus("PENDING")).thenReturn(3L);
        Mockito.when(repository.countByDeliveryStatus("FAILED")).thenReturn(2L);

        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        new OrderOutboxMetrics(repository, registry);

        Assertions.assertEquals(3D, registry.get("trade.outbox.pending").gauge().value());
        Assertions.assertEquals(2D, registry.get("trade.outbox.failed").gauge().value());
    }
}
