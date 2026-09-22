package com.own.promotion.coupon.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CouponReservationCommand {
    private String orderNo;
    private Long buyerId;
    private BigDecimal parentAmount;
    private Map<Long, BigDecimal> merchantAmounts;
    private List<CouponUse> coupons;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public BigDecimal getParentAmount() { return parentAmount; }
    public void setParentAmount(BigDecimal parentAmount) { this.parentAmount = parentAmount; }
    public Map<Long, BigDecimal> getMerchantAmounts() { return merchantAmounts; }
    public void setMerchantAmounts(Map<Long, BigDecimal> merchantAmounts) { this.merchantAmounts = merchantAmounts; }
    public List<CouponUse> getCoupons() { return coupons; }
    public void setCoupons(List<CouponUse> coupons) { this.coupons = coupons; }
}
