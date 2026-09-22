package com.own.promotion.coupon.dto;

import java.math.BigDecimal;

public class CouponUse {
    private String couponNo;
    private Long merchantId;
    private BigDecimal applicableAmount;

    public String getCouponNo() { return couponNo; }
    public void setCouponNo(String couponNo) { this.couponNo = couponNo; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public BigDecimal getApplicableAmount() { return applicableAmount; }
    public void setApplicableAmount(BigDecimal applicableAmount) { this.applicableAmount = applicableAmount; }
}
