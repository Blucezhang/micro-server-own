package com.own.order.service;

import com.own.face.trade.TradeReliabilityMetrics;
import com.own.order.domain.OrderEvent;
import com.own.order.repository.OrderEventRepository;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderOutboxScheduler {
    private final OrderEventRepository eventRepository;
    private final OrderEventSink sink;
    private final int retrySeconds;
    private final int maxAttempts;
    private final TradeReliabilityMetrics metrics;
    public OrderOutboxScheduler(OrderEventRepository eventRepository, OrderEventSink sink,
                                @Value("${trade.outbox.retry-seconds:60}") int retrySeconds,
                                @Value("${trade.outbox.max-attempts:10}") int maxAttempts,
                                TradeReliabilityMetrics metrics) {
        this.eventRepository = eventRepository; this.sink = sink; this.retrySeconds = retrySeconds;
        this.maxAttempts = maxAttempts; this.metrics = metrics;
    }
    @Scheduled(fixedDelayString = "${trade.outbox.delay-millis:60000}")
    @Transactional
    public void deliverPendingEvents() {
        if (!sink.isEnabled()) return;
        for (OrderEvent event : eventRepository.findByDeliveryStatusAndNextAttemptAtBeforeOrderByIdAsc("PENDING", new Date())) {
            try { event.setDeliveryTarget(sink.target()); sink.deliver(event); event.markDelivered(); }
            catch (RuntimeException exception) {
                if (event.getDeliveryAttempts().intValue() + 1 >= maxAttempts) event.markDeliveryFailed(exception.getMessage());
                else event.recordDeliveryFailure(exception.getMessage(), retryDelay(event));
                if (metrics != null) metrics.outboxFailure();
            }
            eventRepository.save(event);
        }
    }
    private int retryDelay(OrderEvent event) {
        int multiplier = 1 << Math.min(event.getDeliveryAttempts().intValue(), 6);
        return retrySeconds * multiplier;
    }
}
