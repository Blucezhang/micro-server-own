package com.own.inventory.controller;

import com.own.face.security.InternalServiceGuard;
import com.own.face.trade.ActorType;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.inventory.repository.InventoryReservationRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/orders")
public class InventoryRecoveryController extends BaseController {
    private final InventoryReservationRepository reservations;
    private final InternalServiceGuard guard;
    public InventoryRecoveryController(InventoryReservationRepository reservations, InternalServiceGuard guard) {
        this.reservations = reservations; this.guard = guard;
    }
    @GetMapping("/{orderNo}/inventory/state")
    @Transactional(readOnly = true)
    public Resp state(@PathVariable String orderNo, HttpServletRequest request) {
        guard.require(request); TradeHeaders.actor(request).require(ActorType.SYSTEM);
        return new Resp(reservations.findByOrderNoOrderByIdAsc(orderNo).stream().map(row -> Map.of(
                "orderNo", row.getOrderNo(), "productId", row.getProductId(), "merchantId", row.getMerchantId(),
                "quantity", row.getQuantity(), "status", row.getStatus().name())).toList());
    }
}
