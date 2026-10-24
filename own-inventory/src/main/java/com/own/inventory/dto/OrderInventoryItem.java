package com.own.inventory.dto;

public class OrderInventoryItem {
    private Long productId;
    private Long merchantId;
    private Integer quantity;

    public OrderInventoryItem() { }

    public OrderInventoryItem(Long productId, Long merchantId, Integer quantity) {
        this.productId = productId;
        this.merchantId = merchantId;
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
