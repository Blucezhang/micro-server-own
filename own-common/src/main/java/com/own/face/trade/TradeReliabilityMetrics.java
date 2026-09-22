package com.own.face.trade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.stereotype.Component;

@Component
public class TradeReliabilityMetrics implements PublicMetrics {
    private final AtomicLong expiredReservations = new AtomicLong();
    private final AtomicLong expiredOrders = new AtomicLong();
    private final AtomicLong outboxFailures = new AtomicLong();
    public void expiredReservations(long count) { expiredReservations.addAndGet(count); }
    public void expiredOrders() { expiredOrders.incrementAndGet(); }
    public void outboxFailure() { outboxFailures.incrementAndGet(); }
    public Collection<Metric<?>> metrics() {
        Collection<Metric<?>> values = new ArrayList<Metric<?>>();
        values.add(new Metric<Long>("trade.inventory.expired-reservations", expiredReservations.get()));
        values.add(new Metric<Long>("trade.order.expired-orders", expiredOrders.get()));
        values.add(new Metric<Long>("trade.outbox.delivery-failures", outboxFailures.get()));
        return values;
    }
}
