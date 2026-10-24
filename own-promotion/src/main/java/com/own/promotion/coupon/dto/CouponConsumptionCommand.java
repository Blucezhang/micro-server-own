package com.own.promotion.coupon.dto;

import java.math.BigDecimal;

public class CouponConsumptionCommand {
    private String orderNo;
    private BigDecimal expectedDiscount;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public BigDecimal getExpectedDiscount() { return expectedDiscount; }
    public void setExpectedDiscount(BigDecimal expectedDiscount) { this.expectedDiscount = expectedDiscount; }
}
