package com.own.order.domain;

import java.math.BigDecimal;
import java.util.Date;
import jakarta.persistence.*;

@Entity @Table(name = "merchant_freight_rule")
public class MerchantFreightRule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "merchant_id", nullable = false, unique = true) private Long merchantId;
    @Column(name = "fixed_amount", nullable = false, precision = 19, scale = 2) private BigDecimal fixedAmount;
    @Column(name = "free_threshold", precision = 19, scale = 2) private BigDecimal freeThreshold;
    @Column(name = "updated_at", nullable = false) private Date updatedAt;
    protected MerchantFreightRule() { }
    public MerchantFreightRule(Long merchantId, BigDecimal fixedAmount, BigDecimal freeThreshold) { this.merchantId = merchantId; update(fixedAmount, freeThreshold); }
    public void update(BigDecimal fixedAmount, BigDecimal freeThreshold) { this.fixedAmount = fixedAmount; this.freeThreshold = freeThreshold; this.updatedAt = new Date(); }
    public BigDecimal freightFor(BigDecimal goodsAmount) { return freeThreshold != null && goodsAmount.compareTo(freeThreshold) >= 0 ? BigDecimal.ZERO : fixedAmount; }
    public Long getMerchantId() { return merchantId; } public BigDecimal getFixedAmount() { return fixedAmount; } public BigDecimal getFreeThreshold() { return freeThreshold; }
}
