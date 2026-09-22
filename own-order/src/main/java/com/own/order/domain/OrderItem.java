package com.own.order.domain;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ord_order_item")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_no", nullable = false, length = 64) private String orderNo;
    @Column(name = "sub_order_no", nullable = false, length = 64) private String subOrderNo;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "merchant_id", nullable = false) private Long merchantId;
    @Column(name = "product_name", nullable = false) private String productName;
    @Column(name = "unit_price", nullable = false, precision = 19, scale = 2) private BigDecimal unitPrice;
    @Column(nullable = false) private Integer quantity;
    @Column(name = "line_total", nullable = false, precision = 19, scale = 2) private BigDecimal lineTotal;

    protected OrderItem() { }
    public OrderItem(String orderNo, String subOrderNo, CartItem item) {
        this.orderNo = orderNo; this.subOrderNo = subOrderNo; this.productId = item.getProductId();
        this.merchantId = item.getMerchantId(); this.productName = item.getProductName();
        this.unitPrice = item.getUnitPrice(); this.quantity = item.getQuantity();
        this.lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity().longValue()));
    }
    public Long getProductId() { return productId; }
    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public String getSubOrderNo() { return subOrderNo; }
    public Long getMerchantId() { return merchantId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
