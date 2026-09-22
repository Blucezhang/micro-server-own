package com.own.inventory.domain;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

/** A merchant-owned threshold for one sellable product. No rule means no alert. */
@Entity
@Table(name = "inv_low_stock_alert_rule", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "merchant_id"}))
public class LowStockAlertRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "threshold_quantity", nullable = false)
    private Integer thresholdQuantity;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    @Column(name = "updated_at", nullable = false)
    private Date updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected LowStockAlertRule() {
    }

    public LowStockAlertRule(Long productId, Long merchantId, Integer thresholdQuantity, Boolean enabled) {
        this.productId = productId;
        this.merchantId = merchantId;
        this.thresholdQuantity = thresholdQuantity;
        this.enabled = enabled;
        this.createdAt = new Date();
        this.updatedAt = this.createdAt;
    }

    public void update(Integer thresholdQuantity, Boolean enabled) {
        this.thresholdQuantity = thresholdQuantity;
        this.enabled = enabled;
        this.updatedAt = new Date();
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Long getMerchantId() { return merchantId; }
    public Integer getThresholdQuantity() { return thresholdQuantity; }
    public Boolean getEnabled() { return enabled; }
    public Date getCreatedAt() { return createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
}
