package com.own.promotion.coupon.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CouponReservationResult {
    private final BigDecimal totalDiscount;
    private final List<String> couponNos;
    private final Map<Long, BigDecimal> merchantDiscounts;

    public CouponReservationResult(BigDecimal totalDiscount, List<String> couponNos,
                                   Map<Long, BigDecimal> merchantDiscounts) {
        this.totalDiscount = totalDiscount;
        this.couponNos = couponNos;
        this.merchantDiscounts = merchantDiscounts;
    }

    public BigDecimal getTotalDiscount() { return totalDiscount; }
    public List<String> getCouponNos() { return couponNos; }
    public Map<Long, BigDecimal> getMerchantDiscounts() { return merchantDiscounts; }
}
