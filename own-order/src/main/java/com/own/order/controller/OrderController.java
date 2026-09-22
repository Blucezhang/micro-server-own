package com.own.order.controller;

import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.order.domain.CartItem;
import com.own.order.domain.TradeOrder;
import com.own.order.domain.TradeSubOrder;
import com.own.order.dto.AddCartItemCommand;
import com.own.order.dto.CheckoutCommand;
import com.own.order.dto.CheckoutQuote;
import com.own.order.dto.ShipmentCommand;
import com.own.order.service.OrderService;
import com.own.order.service.OrderOutboxService;
import com.own.face.trade.ActorType;
import com.own.face.security.InternalServiceGuard;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Date;
import com.own.order.domain.SubOrderStatus;
import com.own.order.dto.FreightRuleCommand;
import com.own.order.dto.LogisticsTraceCommand;
import com.own.order.dto.UpdateCartItemCommand;

@RestController
@RequestMapping("/api/v1")
public class OrderController extends BaseController {

    private final OrderService orderService;
    private final OrderOutboxService outboxService;
    private final InternalServiceGuard internalServiceGuard;

    public OrderController(OrderService orderService, OrderOutboxService outboxService, InternalServiceGuard internalServiceGuard) {
        this.orderService = orderService; this.outboxService = outboxService; this.internalServiceGuard = internalServiceGuard;
    }

    @PostMapping("/cart/items")
    public Resp addCartItem(@RequestBody AddCartItemCommand command, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        TradeHeaders.idempotencyKey(request);
        CartItem item = orderService.addCartItem(actor, command);
        return new Resp(item);
    }

    @GetMapping("/cart/items")
    public Resp cart(HttpServletRequest request) {
        return new Resp(orderService.cart(TradeHeaders.actor(request)));
    }

    @DeleteMapping("/cart/items/{cartItemId}")
    public Resp removeCartItem(@PathVariable Long cartItemId, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        TradeHeaders.idempotencyKey(request);
        orderService.removeCartItem(actor, cartItemId);
        return new Resp(cartItemId);
    }

    @PutMapping("/cart/items/{cartItemId}")
    public Resp updateCartItem(@PathVariable Long cartItemId, @RequestBody UpdateCartItemCommand command,
                               HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        TradeHeaders.idempotencyKey(request);
        return new Resp(orderService.updateCartItem(actor, cartItemId, command));
    }

    @PostMapping("/checkouts/quote")
    public Resp quote(@RequestBody CheckoutCommand command, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        CheckoutQuote quote = orderService.quote(actor, command);
        return new Resp(quote);
    }

    @PostMapping("/orders")
    public Resp createOrder(@RequestBody CheckoutCommand command, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        TradeOrder order = orderService.createOrder(actor, command, TradeHeaders.idempotencyKey(request));
        return new Resp(order, 201, "created");
    }

    @GetMapping("/orders")
    public Resp myOrders(HttpServletRequest request) {
        List<TradeOrder> orders = orderService.findMyOrders(TradeHeaders.actor(request));
        return new Resp(orders);
    }

    @GetMapping("/orders/page")
    public Resp myOrdersPage(@RequestParam(required = false) com.own.order.domain.OrderStatus status, @RequestParam(required = false) Long from,
                             @RequestParam(required = false) Long to, @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size, HttpServletRequest request) {
        return new Resp(orderService.buyerOrderPage(TradeHeaders.actor(request), status, from == null ? null : new Date(from), to == null ? null : new Date(to), page, size));
    }

    @GetMapping("/orders/{orderNo}")
    public Resp order(@PathVariable String orderNo, HttpServletRequest request) {
        return new Resp(orderService.findOrder(TradeHeaders.actor(request), orderNo));
    }

    @GetMapping("/orders/{orderNo}/detail")
    public Resp orderDetail(@PathVariable String orderNo, HttpServletRequest request) {
        return new Resp(orderService.orderDetail(TradeHeaders.actor(request), orderNo));
    }

    @PostMapping("/orders/{orderNo}/cancel")
    public Resp cancel(@PathVariable String orderNo, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        TradeHeaders.idempotencyKey(request);
        return new Resp(orderService.cancel(actor, orderNo));
    }

    @PostMapping("/sub-orders/{subOrderNo}/ship")
    public Resp ship(@PathVariable String subOrderNo, @RequestBody ShipmentCommand command,
                     HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        TradeHeaders.idempotencyKey(request);
        TradeSubOrder subOrder = orderService.ship(actor, subOrderNo, command);
        return new Resp(subOrder);
    }

    @PostMapping("/sub-orders/{subOrderNo}/receive")
    public Resp receive(@PathVariable String subOrderNo, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        TradeHeaders.idempotencyKey(request);
        return new Resp(orderService.receive(actor, subOrderNo));
    }

    @PostMapping("/sub-orders/{subOrderNo}/shipment-correction")
    public Resp correctShipment(@PathVariable String subOrderNo, @RequestBody ShipmentCommand command, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request); TradeHeaders.idempotencyKey(request);
        return new Resp(orderService.correctShipment(actor, subOrderNo, command));
    }

    @PostMapping("/sub-orders/{subOrderNo}/logistics-traces")
    public Resp appendTrace(@PathVariable String subOrderNo, @RequestBody LogisticsTraceCommand command, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request); TradeHeaders.idempotencyKey(request);
        return new Resp(orderService.appendTrace(actor, subOrderNo, command));
    }

    @GetMapping("/sub-orders/{subOrderNo}/logistics-traces")
    public Resp traces(@PathVariable String subOrderNo, HttpServletRequest request) { return new Resp(orderService.logisticsTraces(TradeHeaders.actor(request), subOrderNo)); }

    @PostMapping("/internal/orders/{orderNo}/payment-succeeded")
    public Resp paymentSucceeded(@PathVariable String orderNo, HttpServletRequest request) {
        internalServiceGuard.require(request);
        TradeActor actor = TradeHeaders.actor(request);
        TradeHeaders.idempotencyKey(request);
        return new Resp(orderService.paymentSucceeded(orderNo, actor));
    }

    @PostMapping("/internal/orders/{orderNo}/refund-succeeded")
    public Resp refundSucceeded(@PathVariable String orderNo, HttpServletRequest request) {
        internalServiceGuard.require(request);
        TradeActor actor = TradeHeaders.actor(request);
        TradeHeaders.idempotencyKey(request);
        return new Resp(orderService.refundSucceeded(orderNo, actor));
    }

    @GetMapping("/internal/orders/{orderNo}/settlement-lines")
    public Resp settlementLines(@PathVariable String orderNo, HttpServletRequest request) {
        internalServiceGuard.require(request);
        return new Resp(orderService.settlementLines(TradeHeaders.actor(request), orderNo));
    }

    @GetMapping("/internal/buyers/{buyerId}/products/{productId}/reviewable")
    public Resp reviewable(@PathVariable Long buyerId, @PathVariable Long productId, HttpServletRequest request) {
        internalServiceGuard.require(request);
        return new Resp(orderService.hasReceivedProduct(buyerId, productId));
    }

    @GetMapping("/internal/outbox")
    public Resp outbox(@RequestParam(required = false) String orderNo, @RequestParam(required = false) String status,
                       HttpServletRequest request) {
        internalServiceGuard.require(request);
        TradeHeaders.actor(request).require(ActorType.SYSTEM);
        return new Resp(outboxService.find(orderNo, status));
    }

    @GetMapping("/internal/sensitive-access-audits")
    public Resp sensitiveAccessAudits(@RequestParam(required = false) String orderNo, HttpServletRequest request) {
        internalServiceGuard.require(request);
        return new Resp(orderService.sensitiveAccessAudits(TradeHeaders.actor(request), orderNo));
    }

    @GetMapping("/internal/sensitive-access-audits/page")
    public Resp sensitiveAccessAuditPage(@RequestParam(required = false) String orderNo, @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size, HttpServletRequest request) {
        internalServiceGuard.require(request);
        return new Resp(orderService.sensitiveAccessAuditPage(TradeHeaders.actor(request), orderNo, page, size));
    }

    @PutMapping("/merchant/freight-rule")
    public Resp saveFreightRule(@RequestBody FreightRuleCommand command, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request); TradeHeaders.idempotencyKey(request);
        return new Resp(orderService.saveFreightRule(actor, command));
    }

    @GetMapping("/merchant/freight-rule")
    public Resp freightRule(HttpServletRequest request) { return new Resp(orderService.freightRule(TradeHeaders.actor(request))); }

    @GetMapping("/merchant/sub-orders")
    public Resp merchantSubOrders(@RequestParam(required=false) SubOrderStatus status, @RequestParam(required=false) Long from,
                                  @RequestParam(required=false) Long to, @RequestParam(defaultValue="0") int page,
                                  @RequestParam(defaultValue="20") int size, HttpServletRequest request) {
        return new Resp(orderService.merchantSubOrders(TradeHeaders.actor(request), status, from == null ? null : new Date(from), to == null ? null : new Date(to), page, size));
    }

    @GetMapping("/merchant/sub-orders/{subOrderNo}")
    public Resp merchantSubOrder(@PathVariable String subOrderNo, HttpServletRequest request) { return new Resp(orderService.merchantSubOrderDetail(TradeHeaders.actor(request), subOrderNo)); }
}
