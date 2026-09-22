package com.own.settlement.domain;

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
@Table(name = "pay_payment")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "payment_no", nullable = false, unique = true, length = 64) private String paymentNo;
    @Column(name = "order_no", nullable = false, unique = true, length = 64) private String orderNo;
    @Column(name = "buyer_id", nullable = false) private Long buyerId;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private PaymentStatus status;
    @Column(name = "request_key", nullable = false, unique = true, length = 100) private String requestKey;
    @Enumerated(EnumType.STRING) @Column(name = "payment_channel", nullable = false, length = 32) private PaymentChannel paymentChannel;
    @Column(name = "provider_payment_no", nullable = false, length = 96) private String providerPaymentNo;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    protected Payment() { }
    public Payment(String paymentNo, String orderNo, Long buyerId, BigDecimal amount, String requestKey) {
        this(paymentNo, orderNo, buyerId, amount, requestKey, PaymentChannel.MOCK_WECHAT);
    }
    public Payment(String paymentNo, String orderNo, Long buyerId, BigDecimal amount, String requestKey, PaymentChannel channel) {
        this.paymentNo = paymentNo; this.orderNo = orderNo; this.buyerId = buyerId; this.amount = amount;
        this.requestKey = requestKey; this.paymentChannel = channel; this.providerPaymentNo = providerNo(channel);
        this.status = PaymentStatus.CREATED; this.createdAt = new Date();
    }
    public void succeed() { if (status == PaymentStatus.CREATED) status = PaymentStatus.SUCCEEDED; }
    public void fail() { if (status == PaymentStatus.CREATED) status = PaymentStatus.FAILED; }
    public void refund() { if (status != PaymentStatus.SUCCEEDED) throw new IllegalStateException("payment cannot be refunded"); status = PaymentStatus.REFUNDED; }
    public String getPaymentNo() { return paymentNo; }
    public String getOrderNo() { return orderNo; }
    public Long getBuyerId() { return buyerId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getRequestKey() { return requestKey; }
    public PaymentChannel getPaymentChannel() { return paymentChannel; }
    public String getProviderPaymentNo() { return providerPaymentNo; }
    private String providerNo(PaymentChannel channel) { return (channel == PaymentChannel.MOCK_ALIPAY ? "ALIPAY-" : "WXPAY-") + java.util.UUID.randomUUID().toString(); }
}
