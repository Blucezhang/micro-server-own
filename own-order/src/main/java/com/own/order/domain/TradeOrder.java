package com.own.order.domain;

import com.own.face.trade.recovery.RecoveryStatus;
import java.math.BigDecimal;
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
@Table(name = "ord_order")
public class TradeOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "order_no", nullable = false, unique = true, length = 64) private String orderNo;
    @Column(name = "buyer_id", nullable = false) private Long buyerId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private OrderStatus status;
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2) private BigDecimal totalAmount;
    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2) private BigDecimal discountAmount;
    @Column(name = "freight_amount", nullable = false, precision = 19, scale = 2) private BigDecimal freightAmount;
    @Column(name = "payable_amount", nullable = false, precision = 19, scale = 2) private BigDecimal payableAmount;
    @Column(name = "shipping_address", nullable = false, length = 500) private String shippingAddress;
    @Column(name = "address_id") private Long addressId;
    @Column(name = "recipient_name", length = 64) private String recipientName;
    @Column(name = "recipient_mobile", length = 20) private String recipientMobile;
    @Column(name = "recipient_province", length = 64) private String recipientProvince;
    @Column(name = "recipient_city", length = 64) private String recipientCity;
    @Column(name = "recipient_district", length = 64) private String recipientDistrict;
    @Column(name = "recipient_detail", length = 255) private String recipientDetail;
    @Column(name = "request_key", nullable = false, unique = true, length = 100) private String requestKey;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    @Column(name = "payment_started_at") private Date paymentStartedAt;
    @Column(name = "payment_recovery_attempts", nullable = false) private int paymentRecoveryAttempts;
    @Column(name = "payment_recovery_status", nullable = false, length = 32) private String paymentRecoveryStatus = RecoveryStatus.NONE;
    @Column(name = "cancellation_started_at") private Date cancellationStartedAt;
    @Column(name = "cancellation_recovery_attempts", nullable = false) private int cancellationRecoveryAttempts;
    @Column(name = "cancellation_recovery_status", nullable = false, length = 32) private String cancellationRecoveryStatus = RecoveryStatus.NONE;
    @Column(name = "cancellation_reason", length = 16) private String cancellationReason;

    protected TradeOrder() { }
    /** Compatibility constructor for historical orders and unit tests; new checkouts must use address snapshots. */
    public TradeOrder(String orderNo, Long buyerId, BigDecimal totalAmount, BigDecimal discountAmount,
                      String shippingAddress, String requestKey) {
        this(orderNo, buyerId, totalAmount, discountAmount, shippingAddress, null, null, null, null, null, null, null, BigDecimal.ZERO, requestKey);
    }
    public TradeOrder(String orderNo, Long buyerId, BigDecimal totalAmount, BigDecimal discountAmount,
                      String shippingAddress, Long addressId, String recipientName, String recipientMobile,
                      String recipientProvince, String recipientCity, String recipientDistrict, String recipientDetail,
                      BigDecimal freightAmount, String requestKey) {
        this.orderNo = orderNo; this.buyerId = buyerId; this.totalAmount = totalAmount;
        this.discountAmount = discountAmount; this.freightAmount = freightAmount;
        this.payableAmount = totalAmount.subtract(discountAmount).add(freightAmount);
        this.shippingAddress = shippingAddress; this.requestKey = requestKey; this.status = OrderStatus.PENDING_PAYMENT;
        this.addressId = addressId; this.recipientName = recipientName; this.recipientMobile = recipientMobile;
        this.recipientProvince = recipientProvince; this.recipientCity = recipientCity; this.recipientDistrict = recipientDistrict; this.recipientDetail = recipientDetail;
        this.createdAt = new Date();
    }
    public void markPaid() {
        require(OrderStatus.PENDING_PAYMENT); status = OrderStatus.PAID;
        if (paymentStartedAt != null) paymentRecoveryStatus = RecoveryStatus.COMPLETED;
    }
    public boolean beginPayment(int maxAttempts) {
        require(OrderStatus.PENDING_PAYMENT);
        if (isCancellationStarted()) throw new IllegalStateException("cancellation has already started");
        if (RecoveryStatus.RECONCILIATION_REQUIRED.equals(paymentRecoveryStatus) || paymentRecoveryAttempts >= maxAttempts) {
            paymentRecoveryStatus = RecoveryStatus.RECONCILIATION_REQUIRED;
            return false;
        }
        if (paymentStartedAt == null) paymentStartedAt = new Date();
        paymentRecoveryStatus = RecoveryStatus.PENDING;
        paymentRecoveryAttempts++;
        return true;
    }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isPaymentStarted() { return paymentStartedAt != null; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public int getPaymentRecoveryAttempts() { return paymentRecoveryAttempts; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getPaymentRecoveryStatus() { return paymentRecoveryStatus; }
    public boolean beginCancellation(int maxAttempts, boolean expired) {
        require(OrderStatus.PENDING_PAYMENT);
        if (isPaymentStarted()) throw new IllegalStateException("payment has already started");
        if (RecoveryStatus.RECONCILIATION_REQUIRED.equals(cancellationRecoveryStatus) || cancellationRecoveryAttempts >= maxAttempts) {
            cancellationRecoveryStatus = RecoveryStatus.RECONCILIATION_REQUIRED;
            return false;
        }
        if (cancellationStartedAt == null) {
            cancellationStartedAt = new Date();
            cancellationReason = expired ? CancellationReason.TIMEOUT.name() : CancellationReason.BUYER.name();
        }
        cancellationRecoveryStatus = RecoveryStatus.PENDING;
        cancellationRecoveryAttempts++;
        return true;
    }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public boolean isCancellationStarted() { return cancellationStartedAt != null; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public int getCancellationRecoveryAttempts() { return cancellationRecoveryAttempts; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getCancellationRecoveryStatus() { return cancellationRecoveryStatus; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getCancellationReason() { return cancellationReason; }
    public void startSaga() { require(OrderStatus.PENDING_PAYMENT); status = OrderStatus.PROCESSING; }
    public void sagaReadyForPayment() { require(OrderStatus.PROCESSING); status = OrderStatus.PENDING_PAYMENT; }
    public void sagaFailed() { require(OrderStatus.PROCESSING); status = OrderStatus.SAGA_FAILED; }
    public void markFulfilling() { require(OrderStatus.PAID); status = OrderStatus.FULFILLING; }
    public void markCompleted() { require(OrderStatus.FULFILLING); status = OrderStatus.COMPLETED; }
    public void cancel() {
        require(OrderStatus.PENDING_PAYMENT); status = OrderStatus.CANCELED;
        if (cancellationStartedAt != null) cancellationRecoveryStatus = RecoveryStatus.COMPLETED;
    }
    public void startRefund() { if (status != OrderStatus.PAID && status != OrderStatus.FULFILLING) throw new IllegalStateException("order cannot be refunded"); status = OrderStatus.REFUNDING; }
    public void refund() { require(OrderStatus.REFUNDING); status = OrderStatus.REFUNDED; }
    private void require(OrderStatus expected) { if (status != expected) throw new IllegalStateException("invalid order state transition"); }
    public Long getId() { return id; }
    public String getOrderNo() { return orderNo; }
    public Long getBuyerId() { return buyerId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getFreightAmount() { return freightAmount; }
    public BigDecimal getPayableAmount() { return payableAmount; }
    public String getShippingAddress() { return shippingAddress; }
    public Long getAddressId() { return addressId; }
    public String getRecipientName() { return recipientName; }
    public String getRecipientMobile() { return recipientMobile; }
    public String getRecipientProvince() { return recipientProvince; }
    public String getRecipientCity() { return recipientCity; }
    public String getRecipientDistrict() { return recipientDistrict; }
    public String getRecipientDetail() { return recipientDetail; }
    public String getRequestKey() { return requestKey; }
    public Date getCreatedAt() { return createdAt; }
}
