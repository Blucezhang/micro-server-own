package com.own.order.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.order.domain.OrderStatus;
import com.own.order.domain.TradeOrder;
import com.own.order.repository.TradeOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentRecoveryScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(OrderPaymentRecoveryScheduler.class);
    private final TradeOrderRepository orders;
    private final OrderService service;
    public OrderPaymentRecoveryScheduler(TradeOrderRepository orders, OrderService service) {
        this.orders = orders; this.service = service;
    }
    @Scheduled(fixedDelayString = "${trade.recovery.scan-delay-millis:30000}", scheduler = "tradeRecoveryScheduler")
    public void recover() {
        for (TradeOrder order : orders.findTop50ByStatusAndPaymentRecoveryStatusOrderByPaymentStartedAtAsc(OrderStatus.PENDING_PAYMENT, "PENDING")) {
            try { service.paymentSucceeded(order.getOrderNo(), new TradeActor(1L, ActorType.SYSTEM)); }
            catch (RuntimeException exception) { LOG.warn("Payment recovery failed for {}", order.getOrderNo(), exception); }
        }
    }
}
