package com.own.order.service;

import com.own.order.repository.OrderEventRepository;
import java.util.ArrayList;
import java.util.Collection;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.stereotype.Component;

/** Live outbox gauges: PENDING means persisted but not delivered, never delivery success. */
@Component
public class OrderOutboxMetrics implements PublicMetrics {
    private final OrderEventRepository repository;
    public OrderOutboxMetrics(OrderEventRepository repository) { this.repository = repository; }
    @Override public Collection<Metric<?>> metrics() {
        Collection<Metric<?>> metrics = new ArrayList<Metric<?>>();
        metrics.add(new Metric<Long>("trade.outbox.pending", repository.countByDeliveryStatus("PENDING")));
        metrics.add(new Metric<Long>("trade.outbox.failed", repository.countByDeliveryStatus("FAILED")));
        return metrics;
    }
}
