package com.own.settlement.domain;

import java.math.BigDecimal;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
    @Column(name = "created_at", nullable = false) private Date createdAt;
    protected AfterSaleRefund() { }
    public AfterSaleRefund(String refundNo, String afterSaleNo, Payment payment, BigDecimal amount) {
        this.refundNo = refundNo; this.afterSaleNo = afterSaleNo; this.paymentNo = payment.getPaymentNo();
        this.orderNo = payment.getOrderNo(); this.amount = amount; this.status = "SUCCEEDED"; this.createdAt = new Date();
    }
    public String getRefundNo() { return refundNo; }
    public String getAfterSaleNo() { return afterSaleNo; }
    public String getPaymentNo() { return paymentNo; }
    public String getOrderNo() { return orderNo; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
}
