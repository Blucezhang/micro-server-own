package com.own.order.domain;

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
@Table(name = "ord_sub_order")
public class TradeSubOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "sub_order_no", nullable = false, unique = true, length = 64) private String subOrderNo;
    @Column(name = "order_no", nullable = false, length = 64) private String orderNo;
    @Column(name = "merchant_id", nullable = false) private Long merchantId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24) private SubOrderStatus status;
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2) private BigDecimal totalAmount;
    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2) private BigDecimal discountAmount;
    @Column(name = "freight_amount", nullable = false, precision = 19, scale = 2) private BigDecimal freightAmount;
    @Column(name = "payable_amount", nullable = false, precision = 19, scale = 2) private BigDecimal payableAmount;
    @Column(name = "logistics_company", length = 100) private String logisticsCompany;
    @Column(name = "tracking_no", length = 100) private String trackingNo;
    @Column(name = "shipped_at") private Date shippedAt;

    protected TradeSubOrder() { }
    public TradeSubOrder(String subOrderNo, String orderNo, Long merchantId, BigDecimal totalAmount, BigDecimal discountAmount, BigDecimal freightAmount) {
        this.subOrderNo = subOrderNo; this.orderNo = orderNo; this.merchantId = merchantId;
        this.totalAmount = totalAmount; this.discountAmount = discountAmount; this.freightAmount = freightAmount; this.payableAmount = totalAmount.subtract(discountAmount).add(freightAmount);
        this.status = SubOrderStatus.PENDING_PAYMENT;
    }
    public void markToShip() { require(SubOrderStatus.PENDING_PAYMENT); status = SubOrderStatus.TO_SHIP; }
    public void startSaga() { require(SubOrderStatus.PENDING_PAYMENT); status = SubOrderStatus.PROCESSING; }
    public void sagaReadyForPayment() { require(SubOrderStatus.PROCESSING); status = SubOrderStatus.PENDING_PAYMENT; }
    public void sagaFailed() { require(SubOrderStatus.PROCESSING); status = SubOrderStatus.CANCELED; }
    public void ship(String logisticsCompany, String trackingNo) { require(SubOrderStatus.TO_SHIP); this.logisticsCompany = logisticsCompany; this.trackingNo = trackingNo; this.shippedAt = new Date(); status = SubOrderStatus.SHIPPED; }
    public void correctShipment(String logisticsCompany, String trackingNo) { require(SubOrderStatus.SHIPPED); this.logisticsCompany = logisticsCompany; this.trackingNo = trackingNo; }
    public void receive() { require(SubOrderStatus.SHIPPED); status = SubOrderStatus.RECEIVED; }
    public void cancel() { require(SubOrderStatus.PENDING_PAYMENT); status = SubOrderStatus.CANCELED; }
    public void refund() { if (status != SubOrderStatus.TO_SHIP && status != SubOrderStatus.PENDING_PAYMENT) throw new IllegalStateException("sub order cannot be refunded"); status = SubOrderStatus.REFUNDED; }
    private void require(SubOrderStatus expected) { if (status != expected) throw new IllegalStateException("invalid sub order state transition"); }
    public String getSubOrderNo() { return subOrderNo; }
    public String getOrderNo() { return orderNo; }
    public Long getMerchantId() { return merchantId; }
    public SubOrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getFreightAmount() { return freightAmount; }
    public BigDecimal getPayableAmount() { return payableAmount; }
    public String getLogisticsCompany() { return logisticsCompany; }
    public String getTrackingNo() { return trackingNo; }
    public Date getShippedAt() { return shippedAt; }
}
