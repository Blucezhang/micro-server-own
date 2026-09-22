package com.own.settlement.domain;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pay_refund")
public class Refund {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "refund_no", nullable = false, unique = true, length = 64) private String refundNo;
    @Column(name = "payment_no", nullable = false, unique = true, length = 64) private String paymentNo;
    @Column(name = "order_no", nullable = false, unique = true, length = 64) private String orderNo;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 16) private String status;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    protected Refund() { }
    public Refund(String refundNo, Payment payment) {
        this.refundNo = refundNo; this.paymentNo = payment.getPaymentNo(); this.orderNo = payment.getOrderNo();
        this.amount = payment.getAmount(); this.status = "SUCCEEDED"; this.createdAt = new Date();
    }
    public String getRefundNo() { return refundNo; }
    public String getOrderNo() { return orderNo; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
}
