package com.own.order.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable checkout snapshot used by asynchronous Saga participants. */
public class OrderSagaCommand {
    private String sagaNo;
    private String orderNo;
    private Long buyerId;
    private BigDecimal totalAmount;
    private List<InventoryLine> inventoryLines = new ArrayList<InventoryLine>();
    private List<CouponUse> coupons = new ArrayList<CouponUse>();
    private List<Long> cartItemIds = new ArrayList<Long>();

    public OrderSagaCommand() { }
    public OrderSagaCommand(String sagaNo, String orderNo, Long buyerId, BigDecimal totalAmount,
                            List<InventoryLine> inventoryLines, List<CouponUse> coupons) {
        this.sagaNo = sagaNo; this.orderNo = orderNo; this.buyerId = buyerId; this.totalAmount = totalAmount;
        this.inventoryLines = inventoryLines == null ? new ArrayList<InventoryLine>() : new ArrayList<InventoryLine>(inventoryLines);
        this.coupons = coupons == null ? new ArrayList<CouponUse>() : new ArrayList<CouponUse>(coupons);
    }
    public String getSagaNo() { return sagaNo; }
    public String getOrderNo() { return orderNo; }
    public Long getBuyerId() { return buyerId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<InventoryLine> getInventoryLines() { return Collections.unmodifiableList(inventoryLines); }
    public List<CouponUse> getCoupons() { return Collections.unmodifiableList(coupons); }
    public List<Long> getCartItemIds() { return Collections.unmodifiableList(cartItemIds); }
    public void setSagaNo(String sagaNo) { this.sagaNo = sagaNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setInventoryLines(List<InventoryLine> inventoryLines) { this.inventoryLines = inventoryLines == null ? new ArrayList<InventoryLine>() : new ArrayList<InventoryLine>(inventoryLines); }
    public void setCoupons(List<CouponUse> coupons) { this.coupons = coupons == null ? new ArrayList<CouponUse>() : new ArrayList<CouponUse>(coupons); }
    public void setCartItemIds(List<Long> cartItemIds) { this.cartItemIds = cartItemIds == null ? new ArrayList<Long>() : new ArrayList<Long>(cartItemIds); }

    public static class InventoryLine {
        private Long productId; private Long merchantId; private Integer quantity;
        public InventoryLine() { }
        public InventoryLine(Long productId, Long merchantId, Integer quantity) { this.productId = productId; this.merchantId = merchantId; this.quantity = quantity; }
        public Long getProductId() { return productId; }
        public Long getMerchantId() { return merchantId; }
        public Integer getQuantity() { return quantity; }
        public void setProductId(Long productId) { this.productId = productId; }
        public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
