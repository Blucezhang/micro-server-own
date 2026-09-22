package com.own.order.domain;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ord_cart_item")
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "buyer_id", nullable = false) private Long buyerId;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "merchant_id", nullable = false) private Long merchantId;
    @Column(name = "product_name", nullable = false) private String productName;
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2) private BigDecimal unitPrice;
    @Column(nullable = false) private Integer quantity;

    protected CartItem() { }
    public CartItem(Long buyerId, Long productId, Long merchantId, String productName, BigDecimal unitPrice, Integer quantity) {
        this.buyerId = buyerId; this.productId = productId; this.merchantId = merchantId;
        this.productName = productName; this.unitPrice = unitPrice; this.quantity = quantity;
    }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Long getId() { return id; }
    public Long getBuyerId() { return buyerId; }
    public Long getProductId() { return productId; }
    public Long getMerchantId() { return merchantId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }
}
