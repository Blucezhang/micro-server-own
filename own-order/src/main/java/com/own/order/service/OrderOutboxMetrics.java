package com.own.order.service;

import com.own.order.repository.OrderEventRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/** Live outbox gauges: PENDING means persisted but not delivered, never delivery success. */
@Component
public class OrderOutboxMetrics {
    public OrderOutboxMetrics(OrderEventRepository repository, MeterRegistry registry) {
        Gauge.builder("trade.outbox.pending", repository, value -> count(value, "PENDING")).register(registry);
        Gauge.builder("trade.outbox.failed", repository, value -> count(value, "FAILED")).register(registry);
    }

    private static double count(OrderEventRepository repository, String status) {
        Long value = repository.countByDeliveryStatus(status);
        return value == null ? 0D : value.doubleValue();
    }
}
