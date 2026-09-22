package com.own.settlement.controller;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeHeaders;
import com.own.face.security.InternalServiceGuard;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.settlement.service.MerchantSettlementService;
import java.math.BigDecimal;
import java.util.Collections;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchant/settlement")
public class MerchantSettlementController extends BaseController {
    private final MerchantSettlementService service;
    private final InternalServiceGuard internalServiceGuard;
    public MerchantSettlementController(MerchantSettlementService service, InternalServiceGuard internalServiceGuard) { this.service = service; this.internalServiceGuard = internalServiceGuard; }
    @GetMapping("/balance") public Resp balance(HttpServletRequest request) { return new Resp(Collections.singletonMap("availableAmount", service.availableBalance(merchant(request)))); }
    @PostMapping("/withdrawals") public Resp withdrawal(@RequestBody java.util.Map<String, Object> input, HttpServletRequest request) {
        TradeActor actor = merchant(request); TradeHeaders.idempotencyKey(request);
        Object raw = input == null ? null : input.get("amount"); return new Resp(service.requestWithdrawal(actor, raw == null ? null : new BigDecimal(String.valueOf(raw))), 201, "created");
    }
    @PostMapping("/internal/batches/run") public Resp run(HttpServletRequest request) { internalServiceGuard.require(request); TradeHeaders.actor(request).require(ActorType.SYSTEM); TradeHeaders.idempotencyKey(request); return new Resp(service.settleDue(null)); }
    @PostMapping("/internal/withdrawals/{withdrawalNo}/simulate-paid") public Resp paid(@PathVariable String withdrawalNo, HttpServletRequest request) { internalServiceGuard.require(request); TradeActor actor = TradeHeaders.actor(request); TradeHeaders.idempotencyKey(request); return new Resp(service.simulateWithdrawalPaid(actor, withdrawalNo)); }
    private TradeActor merchant(HttpServletRequest request) { TradeActor actor = TradeHeaders.actor(request); actor.require(ActorType.MERCHANT); return actor; }
}
