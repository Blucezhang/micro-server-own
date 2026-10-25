package com.own.order.service;

import com.own.face.trade.TradeReliabilityMetrics;
import com.own.order.domain.OrderStatus;
import com.own.order.domain.TradeOrder;
import com.own.order.repository.TradeOrderRepository;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderExpiryScheduler {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OrderExpiryScheduler.class);
    private final TradeOrderRepository orderRepository;
    private final OrderService orderService;
    private final int timeoutMinutes;
    private final TradeReliabilityMetrics metrics;
    public OrderExpiryScheduler(TradeOrderRepository orderRepository, OrderService orderService,
                                @Value("${trade.order.payment-timeout-minutes}") int timeoutMinutes,
                                TradeReliabilityMetrics metrics) {
        this.orderRepository = orderRepository; this.orderService = orderService; this.timeoutMinutes = timeoutMinutes;
        this.metrics = metrics;
    }
    @Scheduled(fixedDelayString = "${trade.order.expiry-delay-millis}")
    public void expirePendingOrders() {
        Date deadline = new Date(System.currentTimeMillis() - java.time.Duration.ofMinutes(timeoutMinutes).toMillis());
        for (TradeOrder order : orderRepository.findByStatusAndCreatedAtBefore(OrderStatus.PENDING_PAYMENT, deadline)) {
            try {
                if (orderService.expirePendingOrder(order.getOrderNo(), deadline)) metrics.expiredOrders();
            } catch (RuntimeException exception) {
                LOG.warn("Order expiry deferred for recovery: {}", order.getOrderNo(), exception);
            }
        }
    }
}
