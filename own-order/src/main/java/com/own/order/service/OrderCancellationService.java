package com.own.order.service;

import com.own.face.trade.inbox.OrderEventTypes;
import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.order.domain.OrderEvent;
import com.own.order.domain.OrderStatus;
import com.own.order.domain.TradeOrder;
import com.own.order.domain.TradeSubOrder;
import com.own.order.repository.OrderEventRepository;
import com.own.order.repository.OrderItemRepository;
import com.own.order.repository.TradeOrderRepository;
import com.own.order.repository.TradeSubOrderRepository;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderCancellationService {
    private final OrderCancellationGuard guard;
    private final TradeOrderRepository orders;
    private final TradeSubOrderRepository children;
    private final OrderItemRepository items;
    private final OrderEventRepository events;
    private final InventoryClient inventory;
    private final CouponClient coupons;

    public OrderCancellationService(OrderCancellationGuard guard, TradeOrderRepository orders,
            TradeSubOrderRepository children, OrderItemRepository items, OrderEventRepository events,
            InventoryClient inventory, CouponClient coupons) {
        this.guard = guard; this.orders = orders; this.children = children; this.items = items;
        this.events = events; this.inventory = inventory; this.coupons = coupons;
    }

    @Transactional
    public TradeOrder tryCancel(TradeActor actor, String orderNo, Date deadline) {
        OrderCancellationGuard.Claim claim = guard.claim(actor, orderNo, deadline);
        if (claim == OrderCancellationGuard.Claim.NOT_ELIGIBLE) return null;
        if (claim == OrderCancellationGuard.Claim.RECONCILIATION_REQUIRED) {
            throw TradeException.conflict("order cancellation requires reconciliation");
        }
        TradeOrder order = orders.findLockedByOrderNo(orderNo);
        if (order.getStatus() == OrderStatus.CANCELED) return order;
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT || order.isPaymentStarted()) {
            throw TradeException.conflict("order cannot be canceled from current status");
        }
        if (order.getCancellationRecoveryAttempts() > 1) {
            inventory.reconcileRelease(orderNo, items.findByOrderNo(orderNo));
        } else {
            inventory.release(orderNo);
        }
        // An empty current coupon binding is not proof of historical release. Keep the original key.
        coupons.release(orderNo);
        for (TradeSubOrder child : children.findByOrderNo(orderNo)) {
            child.cancel(); children.save(child);
        }
        boolean expired = com.own.order.domain.CancellationReason.TIMEOUT.name().equals(order.getCancellationReason());
        order.cancel(); orders.save(order);
        events.save(new OrderEvent(orderNo, null, expired ? OrderEventTypes.ORDER_EXPIRED : OrderEventTypes.ORDER_CANCELED,
                expired ? 0L : order.getBuyerId(), expired ? ActorType.SYSTEM.name() : ActorType.BUYER.name(),
                expired ? "payment_timeout" : "unpaid"));
        return order;
    }
}
