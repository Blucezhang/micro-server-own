package com.own.order.service;

import com.own.face.trade.TradeException;
import com.own.order.domain.OrderStatus;
import com.own.order.domain.TradeOrder;
import com.own.order.repository.TradeOrderRepository;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderPaymentGuard {
    private final TradeOrderRepository orders;
    private final int timeoutMinutes;
    private final int maxAttempts;

    public OrderPaymentGuard(TradeOrderRepository orders,
            @Value("${trade.order.payment-timeout-minutes:15}") int timeoutMinutes,
            @Value("${trade.recovery.max-attempts:8}") int maxAttempts) {
        if (timeoutMinutes < 1 || maxAttempts < 1) throw new IllegalArgumentException("positive recovery limits required");
        this.orders = orders; this.timeoutMinutes = timeoutMinutes; this.maxAttempts = maxAttempts;
    }

    /** Must commit before the first remote write; a rollback cannot erase the cancellation barrier. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claim(String orderNo) {
        TradeOrder order = orders.findLockedByOrderNo(orderNo);
        if (order == null) throw TradeException.notFound("order was not found");
        if (java.util.Set.of(OrderStatus.PAID, OrderStatus.FULFILLING, OrderStatus.COMPLETED,
                OrderStatus.REFUNDING, OrderStatus.REFUNDED).contains(order.getStatus())) return true;
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) throw TradeException.conflict("order cannot accept payment");
        if (!order.isPaymentStarted() && order.getCreatedAt().before(new Date(System.currentTimeMillis() - timeoutMinutes * 60_000L))) {
            throw TradeException.conflict("order payment window has expired");
        }
        boolean claimed = order.beginPayment(maxAttempts);
        orders.save(order);
        return claimed;
    }
}
