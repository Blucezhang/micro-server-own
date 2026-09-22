package com.own.inventory.controller;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeHeaders;
import com.own.face.security.InternalServiceGuard;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.inventory.domain.InventoryReservation;
import com.own.inventory.domain.InventoryStock;
import com.own.inventory.dto.InventoryBootstrapRequest;
import com.own.inventory.dto.ReservationCommand;
import com.own.inventory.dto.StockCommand;
import com.own.inventory.dto.InventoryAdjustmentCommand;
import com.own.inventory.dto.AfterSaleInventoryRefundCommand;
import com.own.inventory.dto.LowStockAlertRuleCommand;
import com.own.inventory.service.InventoryService;
import com.own.inventory.service.ProductBootstrapClient;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class InventoryController extends BaseController {

    private final InventoryService inventoryService;
    private final ProductBootstrapClient productBootstrapClient;
    private final InternalServiceGuard internalServiceGuard;

    public InventoryController(InventoryService inventoryService, ProductBootstrapClient productBootstrapClient, InternalServiceGuard internalServiceGuard) {
        this.inventoryService = inventoryService;
        this.productBootstrapClient = productBootstrapClient;
        this.internalServiceGuard = internalServiceGuard;
    }

    @PutMapping("/stocks/{productId}")
    public Resp setStock(@PathVariable Long productId, @RequestBody StockCommand command,
                         HttpServletRequest request) {
        requireSystem(request);
        TradeHeaders.idempotencyKey(request);
        return new Resp(inventoryService.setAvailableStock(productId, command));
    }

    @PostMapping("/stocks/bootstrap")
    public Resp bootstrap(@RequestBody InventoryBootstrapRequest request, HttpServletRequest httpRequest) {
        requireSystem(httpRequest);
        TradeHeaders.idempotencyKey(httpRequest);
        List<InventoryStock> stocks = inventoryService.bootstrap(request == null ? null : request.getProducts());
        return new Resp(stocks);
    }

    @PostMapping("/stocks/bootstrap/catalogue")
    public Resp bootstrapFromCatalogue(HttpServletRequest request) {
        requireSystem(request);
        TradeHeaders.idempotencyKey(request);
        return new Resp(inventoryService.bootstrap(productBootstrapClient.loadCatalogue()));
    }

    @GetMapping("/stocks/{productId}")
    public Resp getStock(@PathVariable Long productId, @RequestParam Long merchantId, HttpServletRequest request) {
        return new Resp(inventoryService.getStock(TradeHeaders.actor(request), productId, merchantId));
    }

    @PostMapping("/stocks/{productId}/adjustments")
    public Resp adjust(@PathVariable Long productId, @RequestBody InventoryAdjustmentCommand command, HttpServletRequest request) {
        TradeActor actor=TradeHeaders.actor(request); TradeHeaders.idempotencyKey(request);
        return new Resp(inventoryService.adjust(productId,actor,command));
    }

    @PutMapping("/stocks/{productId}/low-stock-alert-rule")
    public Resp configureLowStockAlert(@PathVariable Long productId, @RequestBody LowStockAlertRuleCommand command,
                                       HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        TradeHeaders.idempotencyKey(request);
        return new Resp(inventoryService.configureLowStockAlert(productId, actor, command));
    }

    @GetMapping("/merchant/low-stock-alerts")
    public Resp lowStockAlerts(HttpServletRequest request) {
        return new Resp(inventoryService.lowStockAlerts(TradeHeaders.actor(request)));
    }

    @GetMapping("/internal/low-stock-alerts")
    public Resp allLowStockAlerts(HttpServletRequest request) {
        internalServiceGuard.require(request);
        requireSystem(request);
        return new Resp(inventoryService.allLowStockAlerts(TradeHeaders.actor(request)));
    }

    @PostMapping("/internal/reservations")
    public Resp reserve(@RequestBody ReservationCommand command, HttpServletRequest request) {
        internalServiceGuard.require(request);
        requireSystem(request);
        TradeHeaders.idempotencyKey(request);
        InventoryReservation reservation = inventoryService.reserve(command);
        return new Resp(reservation);
    }

    @PostMapping("/internal/orders/{orderNo}/inventory/commit")
    public Resp commit(@PathVariable String orderNo, HttpServletRequest request) {
        internalServiceGuard.require(request);
        requireSystem(request);
        TradeHeaders.idempotencyKey(request);
        inventoryService.commitOrder(orderNo);
        return new Resp(orderNo);
    }

    @PostMapping("/internal/orders/{orderNo}/inventory/release")
    public Resp release(@PathVariable String orderNo, HttpServletRequest request) {
        internalServiceGuard.require(request);
        requireSystem(request);
        TradeHeaders.idempotencyKey(request);
        inventoryService.releaseOrder(orderNo);
        return new Resp(orderNo);
    }

    @PostMapping("/internal/orders/{orderNo}/inventory/refund")
    public Resp refund(@PathVariable String orderNo, HttpServletRequest request) {
        internalServiceGuard.require(request);
        requireSystem(request);
        TradeHeaders.idempotencyKey(request);
        inventoryService.refundOrder(orderNo);
        return new Resp(orderNo);
    }

    @PostMapping("/internal/after-sales/inventory/refund")
    public Resp afterSaleRefund(@RequestBody AfterSaleInventoryRefundCommand command, HttpServletRequest request) { internalServiceGuard.require(request); requireSystem(request); TradeHeaders.idempotencyKey(request); inventoryService.refundAfterSale(command); return new Resp(command.getAfterSaleNo()); }

    @PostMapping("/internal/after-sales/inventory/exchange")
    public Resp afterSaleExchange(@RequestBody AfterSaleInventoryRefundCommand command, HttpServletRequest request) { internalServiceGuard.require(request); requireSystem(request); TradeHeaders.idempotencyKey(request); inventoryService.exchangeAfterSale(command); return new Resp(command.getAfterSaleNo()); }

    @PostMapping("/internal/reservations/expire")
    public Resp expire(HttpServletRequest request) {
        internalServiceGuard.require(request);
        requireSystem(request);
        TradeHeaders.idempotencyKey(request);
        return new Resp(inventoryService.expireReservations());
    }

    private TradeActor requireSystem(HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        actor.require(ActorType.SYSTEM);
        return actor;
    }
}
