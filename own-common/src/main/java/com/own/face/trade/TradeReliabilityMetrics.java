package com.own.face.trade;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class TradeReliabilityMetrics {
    private final Counter expiredReservations;
    private final Counter expiredOrders;
    private final Counter outboxFailures;

    public TradeReliabilityMetrics(MeterRegistry registry) {
        expiredReservations = Counter.builder("trade.inventory.expired-reservations").register(registry);
        expiredOrders = Counter.builder("trade.order.expired-orders").register(registry);
        outboxFailures = Counter.builder("trade.outbox.delivery-failures").register(registry);
    }

    public void expiredReservations(long count) { if (count > 0) expiredReservations.increment(count); }
    public void expiredOrders() { expiredOrders.increment(); }
    public void outboxFailure() { outboxFailures.increment(); }
}
