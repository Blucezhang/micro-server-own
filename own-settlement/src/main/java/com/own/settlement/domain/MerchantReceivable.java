package com.own.settlement.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/** Immutable per-order merchant receivable; status changes express settlement or reversal. */
@Entity
@Table(name = "stl_merchant_receivable")
public class MerchantReceivable {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_no", nullable = false, length = 64) private String orderNo;
    @Column(name = "merchant_id", nullable = false) private Long merchantId;
    @Column(name = "gross_amount", nullable = false, precision = 19, scale = 2) private BigDecimal grossAmount;
    @Column(name = "merchant_amount", nullable = false, precision = 19, scale = 2) private BigDecimal merchantAmount;
    @Column(name = "platform_amount", nullable = false, precision = 19, scale = 2) private BigDecimal platformAmount;
    @Column(nullable = false, length = 24) private String status;
    @Column(name = "settled_batch_no", length = 64) private String settledBatchNo;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    @Column(name = "reversed_at") private Date reversedAt;
    protected MerchantReceivable() { }
    public MerchantReceivable(String orderNo, Long merchantId, BigDecimal grossAmount, BigDecimal merchantRate) {
        this.orderNo = orderNo; this.merchantId = merchantId; this.grossAmount = grossAmount;
        this.merchantAmount = grossAmount.multiply(merchantRate).setScale(2, BigDecimal.ROUND_HALF_UP);
        this.platformAmount = grossAmount.subtract(merchantAmount); this.status = "PENDING"; this.createdAt = new Date();
    }
    public void settle(String batchNo) { if (!"PENDING".equals(status)) throw new IllegalStateException("receivable cannot be settled"); status = "SETTLED"; settledBatchNo = batchNo; }
    /**
     * A buyer refund must not depend on the weekly settlement clock.  Reversing
     * a settled receivable leaves any completed merchant withdrawal in the
     * balance calculation, which makes the resulting recovery amount visible
     * instead of refusing the buyer's refund.
     */
    public void reverse() {
        if (!"PENDING".equals(status) && !"SETTLED".equals(status)) {
            throw new IllegalStateException("receivable cannot be reversed");
        }
        status = "REVERSED";
        reversedAt = new Date();
    }
    public Long getId() { return id; } public String getOrderNo() { return orderNo; } public Long getMerchantId() { return merchantId; }
    public BigDecimal getGrossAmount() { return grossAmount; } public BigDecimal getMerchantAmount() { return merchantAmount; }
    public BigDecimal getPlatformAmount() { return platformAmount; } public String getStatus() { return status; }
    public String getSettledBatchNo() { return settledBatchNo; } public Date getCreatedAt() { return createdAt; }
}
