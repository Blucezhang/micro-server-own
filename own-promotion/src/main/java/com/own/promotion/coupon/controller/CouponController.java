package com.own.promotion.coupon.controller;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeHeaders;
import com.own.face.security.InternalServiceGuard;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.promotion.coupon.domain.UserCoupon;
import com.own.promotion.coupon.dto.ClaimCouponCommand;
import com.own.promotion.coupon.dto.CouponReservationCommand;
import com.own.promotion.coupon.dto.CouponReservationResult;
import com.own.promotion.coupon.dto.MerchantCouponTemplateCommand;
import com.own.promotion.coupon.domain.MerchantCouponTemplate;
import com.own.promotion.coupon.service.CouponService;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sale/api/v1/coupons")
public class CouponController extends BaseController {

    private final CouponService couponService;
    private final InternalServiceGuard internalServiceGuard;

    public CouponController(CouponService couponService, InternalServiceGuard internalServiceGuard) {
        this.couponService = couponService;
        this.internalServiceGuard = internalServiceGuard;
    }

    @PostMapping("/claim")
    public Resp claim(@RequestBody ClaimCouponCommand command, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        actor.require(ActorType.BUYER);
        TradeHeaders.idempotencyKey(request);
        return new Resp(couponService.claim(actor.getId(), command));
    }

    @GetMapping("/me")
    public Resp mine(HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        actor.require(ActorType.BUYER);
        List<UserCoupon> coupons = couponService.findByBuyer(actor.getId());
        return new Resp(coupons);
    }

    @GetMapping("/merchant/summary")
    public Resp merchantSummary(HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request);
        actor.require(ActorType.MERCHANT);
        Map<String, Object> result = couponService.merchantSummary(actor.getId());
        return new Resp(result);
    }

    @GetMapping("/merchant/templates")
    public Resp merchantTemplates(HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request); actor.require(ActorType.MERCHANT);
        return new Resp(couponService.merchantTemplates(actor.getId()));
    }

    @PostMapping("/merchant/templates")
    public Resp createMerchantTemplate(@RequestBody MerchantCouponTemplateCommand command, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request); actor.require(ActorType.MERCHANT); TradeHeaders.idempotencyKey(request);
        MerchantCouponTemplate created = couponService.createMerchantTemplate(actor.getId(), command);
        return new Resp(created, 201, "created");
    }

    @PostMapping("/merchant/templates/{templateId}/status")
    public Resp merchantTemplateStatus(@PathVariable Long templateId, @RequestParam String value, HttpServletRequest request) {
        TradeActor actor = TradeHeaders.actor(request); actor.require(ActorType.MERCHANT); TradeHeaders.idempotencyKey(request);
        return new Resp(couponService.changeMerchantTemplateStatus(actor.getId(), templateId, value));
    }

    @PostMapping("/internal/reservations")
    public Resp reserve(@RequestBody CouponReservationCommand command, HttpServletRequest request) {
        requireSystem(request);
        TradeHeaders.idempotencyKey(request);
        CouponReservationResult result = couponService.reserve(command);
        return new Resp(result);
    }

    @PostMapping("/internal/quotes")
    public Resp preview(@RequestBody CouponReservationCommand command, HttpServletRequest request) {
        requireSystem(request);
        return new Resp(couponService.preview(command));
    }

    @PostMapping("/internal/orders/consume")
    public Resp consume(@RequestBody String orderNo, HttpServletRequest request) {
        requireSystem(request);
        TradeHeaders.idempotencyKey(request);
        couponService.consumeOrder(orderNo);
        return new Resp(orderNo);
    }

    @PostMapping("/internal/orders/release")
    public Resp release(@RequestBody String orderNo, HttpServletRequest request) {
        requireSystem(request);
        TradeHeaders.idempotencyKey(request);
        couponService.releaseOrder(orderNo);
        return new Resp(orderNo);
    }

    private void requireSystem(HttpServletRequest request) {
        internalServiceGuard.require(request);
        TradeHeaders.actor(request).require(ActorType.SYSTEM);
    }
}
