package com.own.order.controller;

import com.own.face.trade.recovery.RecoveryStatus;
import com.own.face.security.InternalServiceGuard;
import com.own.face.trade.ActorType;
import com.own.face.trade.TradeException;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.order.repository.TradeOrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/recovery/cancellations")
public class OrderCancellationRecoveryController {
    private final TradeOrderRepository orders;
    private final InternalServiceGuard guard;
    public OrderCancellationRecoveryController(TradeOrderRepository orders, InternalServiceGuard guard) {
        this.orders = orders; this.guard = guard;
    }
    @GetMapping
    public Resp list(@RequestParam(defaultValue = RecoveryStatus.RECONCILIATION_REQUIRED) String status, HttpServletRequest request) {
        guard.require(request); TradeHeaders.actor(request).require(ActorType.SYSTEM);
        if (!RecoveryStatus.QUERYABLE.contains(status)) {
            throw TradeException.unprocessable("invalid cancellation recovery status");
        }
        return new Resp(orders.findTop100ByCancellationRecoveryStatusOrderByIdDesc(status).stream().map(order ->
                new CancellationView(order.getOrderNo(), order.getStatus().name(), order.getCancellationRecoveryStatus(),
                        order.getCancellationRecoveryAttempts(), order.getCancellationReason())).toList());
    }
    public record CancellationView(String orderNo, String status, String recoveryStatus, int attempts, String reason) { }
}
