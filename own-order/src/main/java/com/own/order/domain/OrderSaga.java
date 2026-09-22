package com.own.order.domain;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "trade_order_saga")
public class OrderSaga {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "saga_no", nullable = false, unique = true, length = 64) private String sagaNo;
    @Column(name = "order_no", nullable = false, unique = true, length = 64) private String orderNo;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private OrderSagaStatus status;
    @Enumerated(EnumType.STRING) @Column(name = "current_step", nullable = false, length = 32) private OrderSagaStep currentStep;
    @Column(name = "command_payload", nullable = false, columnDefinition = "MEDIUMTEXT") private String commandPayload;
    @Column(name = "attempts", nullable = false) private int attempts;
    @Column(name = "last_error", length = 500) private String lastError;
    @Column(name = "next_attempt_at") private Date nextAttemptAt;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    @Column(name = "updated_at", nullable = false) private Date updatedAt;

    protected OrderSaga() { }
    public OrderSaga(String sagaNo, String orderNo, String commandPayload) {
        this.sagaNo = sagaNo; this.orderNo = orderNo; this.commandPayload = commandPayload;
        this.status = OrderSagaStatus.PROCESSING; this.currentStep = OrderSagaStep.RESERVE_INVENTORY;
        this.createdAt = new Date(); this.updatedAt = createdAt;
    }
    public String getSagaNo() { return sagaNo; }
    public String getOrderNo() { return orderNo; }
    public OrderSagaStatus getStatus() { return status; }
    public OrderSagaStep getCurrentStep() { return currentStep; }
    public String getCommandPayload() { return commandPayload; }
    public int getAttempts() { return attempts; }
    public String getLastError() { return lastError; }
    public Date getNextAttemptAt() { return nextAttemptAt; }
    public Date getCreatedAt() { return createdAt; }
    public void advance(OrderSagaStep next) { ensure(OrderSagaStatus.PROCESSING); currentStep = next; attempts = 0; lastError = null; nextAttemptAt = new Date(); touch(); }
    public void complete() { ensure(OrderSagaStatus.PROCESSING); status = OrderSagaStatus.COMPLETED; currentStep = OrderSagaStep.READY_FOR_PAYMENT; touch(); }
    public void compensate(String error, OrderSagaStep step) { ensure(OrderSagaStatus.PROCESSING); status = OrderSagaStatus.COMPENSATING; currentStep = step; lastError = truncate(error); nextAttemptAt = new Date(); touch(); }
    public void fail() { ensure(OrderSagaStatus.COMPENSATING); status = OrderSagaStatus.FAILED; touch(); }
    public void retry(String error, int delaySeconds) {
        attempts++;
        lastError = truncate(error);
        nextAttemptAt = new Date(System.currentTimeMillis() + Math.max(1, delaySeconds) * 1000L);
        touch();
    }
    private void ensure(OrderSagaStatus expected) { if (status != expected) throw new IllegalStateException("invalid order saga transition"); }
    private void touch() { updatedAt = new Date(); }
    private String truncate(String value) { return value == null ? null : value.substring(0, Math.min(value.length(), 500)); }
}
