package com.own.promotion.coupon.dto;

public class ClaimCouponCommand {
    private String couponType;
    private Long ticketId;
    private Long merchantId;
    private Long merchantTemplateId;

    public String getCouponType() { return couponType; }
    public void setCouponType(String couponType) { this.couponType = couponType; }
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Long getMerchantTemplateId() { return merchantTemplateId; }
    public void setMerchantTemplateId(Long merchantTemplateId) { this.merchantTemplateId = merchantTemplateId; }
}
