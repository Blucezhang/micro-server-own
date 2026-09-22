package com.own.order.controller;

import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeHeaders;
import com.own.face.security.InternalServiceGuard;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.order.service.AfterSaleService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Internal settlement callback. The service enforces the SYSTEM actor. */
@RestController
@RequestMapping("/api/v1/internal/after-sales")
public class AfterSaleInternalController extends BaseController {
    private final AfterSaleService service;
    private final InternalServiceGuard internalServiceGuard;
    public AfterSaleInternalController(AfterSaleService service, InternalServiceGuard internalServiceGuard) { this.service = service; this.internalServiceGuard = internalServiceGuard; }
    @PostMapping("/{afterSaleNo}/refund-succeeded")
    public Resp refundSucceeded(@PathVariable String afterSaleNo, HttpServletRequest request) {
        internalServiceGuard.require(request);
        TradeActor actor = TradeHeaders.actor(request); TradeHeaders.idempotencyKey(request);
        return new Resp(service.refundSucceeded(actor, afterSaleNo));
    }
}
