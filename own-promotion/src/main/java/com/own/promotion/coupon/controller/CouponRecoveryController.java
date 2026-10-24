package com.own.promotion.coupon.controller;

import com.own.face.security.InternalServiceGuard;
import com.own.face.trade.ActorType;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.promotion.coupon.repository.UserCouponRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sale/api/v1/coupons/internal/orders")
public class CouponRecoveryController extends BaseController {
    private final UserCouponRepository coupons;
    private final InternalServiceGuard guard;
    public CouponRecoveryController(UserCouponRepository coupons, InternalServiceGuard guard) {
        this.coupons = coupons; this.guard = guard;
    }
    @GetMapping("/{orderNo}/state")
    @Transactional(readOnly = true)
    public Resp state(@PathVariable String orderNo, HttpServletRequest request) {
        guard.require(request); TradeHeaders.actor(request).require(ActorType.SYSTEM);
        return new Resp(coupons.findByReservedOrderNoOrderByIdAsc(orderNo).stream().map(row -> Map.of(
                "couponNo", row.getCouponNo(), "buyerId", row.getBuyerId(), "orderNo", row.getReservedOrderNo(),
                "status", row.getStatus().name(), "discountAmount", row.getDiscountAmount())).toList());
    }
}
