package com.own.inventory.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/** Ledger guard preventing a returned line from being restocked more than once. */
@Entity @Table(name = "inv_after_sale_refund")
public class AfterSaleInventoryRefund {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "after_sale_no", nullable = false, length = 64) private String afterSaleNo;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "merchant_id", nullable = false) private Long merchantId;
    @Column(nullable = false) private Integer quantity;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    protected AfterSaleInventoryRefund() { }
    public AfterSaleInventoryRefund(String afterSaleNo, Long productId, Long merchantId, Integer quantity) { this.afterSaleNo = afterSaleNo; this.productId = productId; this.merchantId = merchantId; this.quantity = quantity; this.createdAt = new Date(); }
    public String getAfterSaleNo() { return afterSaleNo; } public Long getProductId() { return productId; } public Long getMerchantId() { return merchantId; } public Integer getQuantity() { return quantity; }
}
