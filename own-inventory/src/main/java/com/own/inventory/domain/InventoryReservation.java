package com.own.inventory.domain;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "inv_reservation", uniqueConstraints = @UniqueConstraint(columnNames = {"order_no", "product_id"}))
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservation_no", nullable = false, unique = true, length = 64)
    private String reservationNo;

    @Column(name = "order_no", nullable = false, length = 64)
    private String orderNo;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private Date expiresAt;

    protected InventoryReservation() {
    }

    public InventoryReservation(String reservationNo, String orderNo, Long productId, Long merchantId,
                                Integer quantity, Date expiresAt) {
        this.reservationNo = reservationNo;
        this.orderNo = orderNo;
        this.productId = productId;
        this.merchantId = merchantId;
        this.quantity = quantity;
        this.status = ReservationStatus.RESERVED;
        this.expiresAt = expiresAt;
    }

    public void commit() { this.status = ReservationStatus.COMMITTED; }
    public void release() { this.status = ReservationStatus.RELEASED; }
    public void refund() { this.status = ReservationStatus.REFUNDED; }
    public void expire() { this.status = ReservationStatus.EXPIRED; }
    public String getReservationNo() { return reservationNo; }
    public String getOrderNo() { return orderNo; }
    public Long getProductId() { return productId; }
    public Long getMerchantId() { return merchantId; }
    public Integer getQuantity() { return quantity; }
    public ReservationStatus getStatus() { return status; }
    public Date getExpiresAt() { return expiresAt; }
}
