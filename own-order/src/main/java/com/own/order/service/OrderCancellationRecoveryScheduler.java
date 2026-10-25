package com.own.order.service;

import com.own.face.trade.recovery.RecoveryStatus;
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
public class OrderCancellationRecoveryScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(OrderCancellationRecoveryScheduler.class);
    private final TradeOrderRepository orders;
    private final OrderCancellationService service;
    public OrderCancellationRecoveryScheduler(TradeOrderRepository orders, OrderCancellationService service) {
        this.orders = orders; this.service = service;
    }
    @Scheduled(fixedDelayString = "${trade.recovery.scan-delay-millis}", scheduler = "tradeRecoveryScheduler")
    public void recover() {
        for (TradeOrder order : orders.findTop50ByStatusAndCancellationRecoveryStatusOrderByCancellationStartedAtAsc(
                OrderStatus.PENDING_PAYMENT, RecoveryStatus.PENDING)) {
            try { service.tryCancel(new TradeActor(0L, ActorType.SYSTEM), order.getOrderNo(), null); }
            catch (RuntimeException exception) { LOG.warn("Cancellation recovery failed for {}", order.getOrderNo(), exception); }
        }
    }
}
