package com.own.settlement.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import com.own.face.trade.TradeException;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;

/** A refund for one completed after-sale request. It is separate from whole-order Refund. */
@Entity
@Table(name = "pay_after_sale_refund")
public class AfterSaleRefund {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "refund_no", nullable = false, unique = true, length = 64) private String refundNo;
    @Column(name = "after_sale_no", nullable = false, unique = true, length = 64) private String afterSaleNo;
    @Column(name = "payment_no", nullable = false, length = 64) private String paymentNo;
    @Column(name = "order_no", nullable = false, length = 64) private String orderNo;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 16) private String status;
    @Column(name = "merchant_id") private Long merchantId;
    @Column(name = "merchant_refund_amount", precision = 19, scale = 2) private BigDecimal merchantRefundAmount;
    @Column(name = "platform_refund_amount", precision = 19, scale = 2) private BigDecimal platformRefundAmount;
    @Column(name = "reconciliation_status", nullable = false, length = 32) private String reconciliationStatus;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    protected AfterSaleRefund() { }
    public AfterSaleRefund(String refundNo, String afterSaleNo, Payment payment, Long merchantId, BigDecimal amount) {
        if (merchantId == null || merchantId <= 0) throw TradeException.unprocessable("merchantId is required");
        this.refundNo = refundNo; this.afterSaleNo = afterSaleNo; this.paymentNo = payment.getPaymentNo();
        this.orderNo = payment.getOrderNo(); this.amount = amount; this.status = "SUCCEEDED"; this.createdAt = new Date();
        this.merchantId = merchantId;
        this.reconciliationStatus = "PENDING_RECONCILIATION";
    }

    public void allocate(MerchantReceivable receivable, BigDecimal priorGross, BigDecimal priorMerchant) {
        if ("APPLIED".equals(reconciliationStatus)) throw TradeException.conflict("refund allocation is immutable");
        if (receivable == null || !orderNo.equals(receivable.getOrderNo())
                || !merchantId.equals(receivable.getMerchantId())
                || !("PENDING".equals(receivable.getStatus()) || "SETTLED".equals(receivable.getStatus()))) {
            throw TradeException.conflict("refund does not match an active merchant receivable");
        }
        BigDecimal gross = receivable.getGrossAmount();
        BigDecimal merchant = receivable.getMerchantAmount();
        if (gross.signum() <= 0 || merchant.signum() < 0 || merchant.compareTo(gross) > 0
                || merchant.add(receivable.getPlatformAmount()).compareTo(gross) != 0) {
            throw TradeException.conflict("original merchant receivable split is invalid");
        }
        BigDecimal cumulativeGross = priorGross.add(amount);
        if (amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 2 || priorGross.signum() < 0
                || cumulativeGross.compareTo(gross) > 0
                || priorMerchant.compareTo(priorGross.multiply(merchant).divide(gross, 2, RoundingMode.HALF_UP)) != 0) {
            throw TradeException.conflict("refund exceeds or does not match original merchant receivable");
        }
        // Cumulative rounding returns the original split exactly, including the last cent.
        merchantRefundAmount = cumulativeGross.multiply(merchant).divide(gross, 2, RoundingMode.HALF_UP).subtract(priorMerchant);
        platformRefundAmount = amount.subtract(merchantRefundAmount);
        reconciliationStatus = "APPLIED";
    }

    @PrePersist
    private void requireAllocatedNewRefund() {
        if (!"APPLIED".equals(reconciliationStatus) || merchantId == null || merchantId <= 0
                || merchantRefundAmount == null || platformRefundAmount == null
                || merchantRefundAmount.signum() < 0 || platformRefundAmount.signum() < 0
                || merchantRefundAmount.add(platformRefundAmount).compareTo(amount) != 0) {
            throw new IllegalStateException("new after-sale refund must include its merchant allocation");
        }
    }
    public String getRefundNo() { return refundNo; }
    public String getAfterSaleNo() { return afterSaleNo; }
    public String getPaymentNo() { return paymentNo; }
    public String getOrderNo() { return orderNo; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public Long getMerchantId() { return merchantId; }
    public BigDecimal getMerchantRefundAmount() { return merchantRefundAmount; }
    public BigDecimal getPlatformRefundAmount() { return platformRefundAmount; }
    public String getReconciliationStatus() { return reconciliationStatus; }
}
