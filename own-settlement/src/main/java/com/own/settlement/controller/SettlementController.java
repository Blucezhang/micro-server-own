package com.own.settlement.controller;

import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.settlement.dto.CreatePaymentCommand;
import com.own.settlement.dto.AfterSaleRefundCommand;
import com.own.settlement.dto.MockPaymentCallbackCommand;
import com.own.face.trade.ActorType;
import com.own.face.security.InternalServiceGuard;
import com.own.settlement.service.SettlementService;
import com.own.settlement.service.MockPaymentCallbackGuard;
import com.own.settlement.domain.PaymentChannel;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
public class SettlementController extends BaseController {
    private final SettlementService settlementService;
    private final InternalServiceGuard internalServiceGuard;
    private final MockPaymentCallbackGuard mockPaymentCallbackGuard;
    public SettlementController(SettlementService settlementService, InternalServiceGuard internalServiceGuard, MockPaymentCallbackGuard mockPaymentCallbackGuard) { this.settlementService = settlementService; this.internalServiceGuard = internalServiceGuard; this.mockPaymentCallbackGuard = mockPaymentCallbackGuard; }

    @PostMapping
    public Resp create(@RequestBody CreatePaymentCommand command, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        return new Resp(settlementService.createPayment(actor, command, TradeHeaders.idempotencyKey(request)), 201, "created");
    }

    @GetMapping("/{paymentNo}")
    public Resp find(@PathVariable String paymentNo, HttpServletRequest request) {
        return new Resp(settlementService.find(TradeHeaders.actor(request), paymentNo));
    }

    @PostMapping("/{paymentNo}/simulate-success")
    public Resp success(@PathVariable String paymentNo, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request); TradeHeaders.idempotencyKey(request);
        return new Resp(settlementService.simulateSuccess(actor, paymentNo));
    }

    @PostMapping("/{paymentNo}/simulate-failure")
    public Resp failure(@PathVariable String paymentNo, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request); TradeHeaders.idempotencyKey(request);
        return new Resp(settlementService.simulateFailure(actor, paymentNo));
    }

    @PostMapping("/mock-callbacks/{channel}")
    public Resp mockCallback(@PathVariable String channel, @RequestBody MockPaymentCallbackCommand command, HttpServletRequest request) {
        mockPaymentCallbackGuard.require(request);
        return new Resp(settlementService.processMockCallback(PaymentChannel.parse(channel), command));
    }

    @PostMapping("/orders/{orderNo}/refund")
    public Resp refund(@PathVariable String orderNo, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request); TradeHeaders.idempotencyKey(request);
        return new Resp(settlementService.refund(actor, orderNo));
    }

    @PostMapping("/internal/after-sales/refund")
    public Resp afterSaleRefund(@RequestBody AfterSaleRefundCommand command, HttpServletRequest request) {
        internalServiceGuard.require(request);
        TradeActor actor = TradeHeaders.actor(request); actor.require(ActorType.SYSTEM); TradeHeaders.idempotencyKey(request);
        return new Resp(settlementService.refundAfterSale(actor, command));
    }
}
