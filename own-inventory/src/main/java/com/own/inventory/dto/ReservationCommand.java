package com.own.inventory.dto;

public class ReservationCommand {
    private String orderNo;
    private Long productId;
    private Long merchantId;
    private Integer quantity;
    private Integer ttlMinutes;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getTtlMinutes() { return ttlMinutes; }
    public void setTtlMinutes(Integer ttlMinutes) { this.ttlMinutes = ttlMinutes; }
}
