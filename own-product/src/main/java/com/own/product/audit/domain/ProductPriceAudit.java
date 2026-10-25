package com.own.product.audit.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "prd_product_price_audit")
public class ProductPriceAudit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @JsonIgnore @Column(name = "event_key", length = 64, unique = true) private String eventKey;
    @Column(name = "product_id", nullable = false) private Long productId;
    @Column(name = "merchant_id", nullable = false) private Long merchantId;
    @Column(name = "original_price_before", nullable = false) private BigDecimal originalPriceBefore;
    @Column(name = "original_price_after", nullable = false) private BigDecimal originalPriceAfter;
    @Column(name = "promotion_price_before") private BigDecimal promotionPriceBefore;
    @Column(name = "promotion_price_after") private BigDecimal promotionPriceAfter;
    @Column(length = 200) private String reason;
    @Column(name = "changed_at", nullable = false) private Date changedAt;

    protected ProductPriceAudit() { }

    public ProductPriceAudit(Long productId, Long merchantId, BigDecimal originalBefore, BigDecimal originalAfter, BigDecimal promotionBefore, BigDecimal promotionAfter, String reason) {
        this(null, productId, merchantId, originalBefore, originalAfter, promotionBefore, promotionAfter, reason, wholeSecondDate(new Date()));
    }

    public ProductPriceAudit(String eventKey, Long productId, Long merchantId, BigDecimal originalBefore, BigDecimal originalAfter, BigDecimal promotionBefore, BigDecimal promotionAfter, String reason, Date changedAt) {
        this.eventKey = eventKey;
        this.productId = productId;
        this.merchantId = merchantId;
        this.originalPriceBefore = originalBefore;
        this.originalPriceAfter = originalAfter;
        this.promotionPriceBefore = promotionBefore;
        this.promotionPriceAfter = promotionAfter;
        this.reason = reason;
        this.changedAt = wholeSecondDate(changedAt == null ? new Date() : changedAt);
    }

    private static Date wholeSecondDate(Date value) {
        return new Date((value.getTime() / 1000L) * 1000L);
    }

    public Long getId() { return id; }
    @JsonIgnore public String getEventKey() { return eventKey; }
    public Long getProductId() { return productId; }
    public Long getMerchantId() { return merchantId; }
    public BigDecimal getOriginalPriceBefore() { return originalPriceBefore; }
    public BigDecimal getOriginalPriceAfter() { return originalPriceAfter; }
    public BigDecimal getPromotionPriceBefore() { return promotionPriceBefore; }
    public BigDecimal getPromotionPriceAfter() { return promotionPriceAfter; }
    public String getReason() { return reason; }
    public Date getChangedAt() { return changedAt; }
}
