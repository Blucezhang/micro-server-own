package com.own.order.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.order.domain.OrderStatus;
import com.own.order.domain.TradeOrder;
import com.own.order.repository.TradeOrderRepository;
import java.util.Date;
import com.own.face.trade.config.TradeRecoveryProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderCancellationGuard {
    public enum Claim { NOT_ELIGIBLE, CLAIMED, RECONCILIATION_REQUIRED }
    private final TradeOrderRepository orders;
    private final int maxAttempts;

    public OrderCancellationGuard(TradeOrderRepository orders,
            TradeRecoveryProperties recovery) {
        this.orders = orders; this.maxAttempts = recovery.maxAttempts();
    }

    /** Commit cancellation ownership before any remote resource can be released. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Claim claim(TradeActor actor, String orderNo, Date deadline) {
        if (actor.getType() != ActorType.BUYER && actor.getType() != ActorType.SYSTEM) {
            throw TradeException.forbidden("buyer or system actor required");
        }
        TradeOrder order = orders.findLockedByOrderNo(orderNo);
        if (order == null || actor.getType() == ActorType.BUYER && !actor.getId().equals(order.getBuyerId())) {
            throw TradeException.notFound("order was not found");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT || order.isPaymentStarted()
                || deadline != null && !order.getCreatedAt().before(deadline)) return Claim.NOT_ELIGIBLE;
        boolean claimed = order.beginCancellation(maxAttempts, deadline != null);
        orders.save(order);
        return claimed ? Claim.CLAIMED : Claim.RECONCILIATION_REQUIRED;
    }
}
