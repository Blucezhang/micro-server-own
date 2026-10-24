package com.own.settlement.domain;

import com.own.face.trade.TradeException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "stl_recovery_operation")
public class RecoveryOperation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "operation_key", nullable = false, unique = true, length = 100) private String operationKey;
    @Column(name = "payment_no", nullable = false, length = 64) private String paymentNo;
    @Column(name = "order_no", nullable = false, length = 64) private String orderNo;
    @Column(name = "buyer_id", nullable = false) private Long buyerId;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private RecoveryAction action;
    @Column(nullable = false, length = 32) private String status;
    @Column(nullable = false) private int attempts;
    @Column(name = "next_attempt_at", nullable = false) private Date nextAttemptAt;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    @Column(name = "completed_at") private Date completedAt;
    protected RecoveryOperation() { }
    public RecoveryOperation(Payment payment, RecoveryAction action) {
        this.operationKey = key(payment.getPaymentNo(), action); this.paymentNo = payment.getPaymentNo();
        this.orderNo = payment.getOrderNo(); this.buyerId = payment.getBuyerId(); this.amount = payment.getAmount();
        this.action = action; this.status = "PENDING"; this.createdAt = new Date(); this.nextAttemptAt = createdAt;
    }
    public static String key(String paymentNo, RecoveryAction action) { return action.name() + ":" + paymentNo; }
    public void requireMatches(Payment payment) {
        if (!paymentNo.equals(payment.getPaymentNo()) || !orderNo.equals(payment.getOrderNo())
                || !buyerId.equals(payment.getBuyerId()) || amount.compareTo(payment.getAmount()) != 0) {
            throw TradeException.conflict("recovery operation does not match original payment");
        }
    }
    public int attempt(int maxAttempts, long retryMillis, Date now) {
        if ("COMPLETED".equals(status)) return attempts;
        if ("RECONCILIATION_REQUIRED".equals(status) || attempts >= maxAttempts) {
            status = "RECONCILIATION_REQUIRED";
            return 0;
        }
        attempts++; nextAttemptAt = new Date(now.getTime() + retryMillis);
        return attempts;
    }
    public void complete() { status = "COMPLETED"; if (completedAt == null) completedAt = new Date(); }
    public String getOperationKey() { return operationKey; }
    public String getPaymentNo() { return paymentNo; }
    public String getOrderNo() { return orderNo; }
    public Long getBuyerId() { return buyerId; }
    public BigDecimal getAmount() { return amount; }
    public RecoveryAction getAction() { return action; }
    public String getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public Date getNextAttemptAt() { return nextAttemptAt; }
}
