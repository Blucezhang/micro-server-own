package com.own.promotion.coupon.domain;

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
@Table(name = "mkt_user_coupon")
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coupon_no", nullable = false, unique = true, length = 64)
    private String couponNo;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false, length = 16)
    private CouponType couponType;

    @Column(name = "legacy_ticket_id", nullable = false)
    private Long legacyTicketId;

    @Column(name = "source_key", length = 128)
    private String sourceKey;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "minimum_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal minimumAmount;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "expires_at", nullable = false)
    private Date expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CouponStatus status;

    @Column(name = "reserved_order_no", length = 64)
    private String reservedOrderNo;

    @Column(name = "reservation_expires_at")
    private Date reservationExpiresAt;

    protected UserCoupon() {
    }

    public UserCoupon(String couponNo, Long buyerId, CouponType couponType, Long legacyTicketId,
                      Long merchantId, BigDecimal minimumAmount, BigDecimal discountAmount, Date expiresAt) {
        this(couponNo, buyerId, couponType, legacyTicketId, merchantId, minimumAmount, discountAmount, expiresAt, null);
    }

    public UserCoupon(String couponNo, Long buyerId, CouponType couponType, Long legacyTicketId,
                      Long merchantId, BigDecimal minimumAmount, BigDecimal discountAmount, Date expiresAt, String sourceKey) {
        this.couponNo = couponNo;
        this.buyerId = buyerId;
        this.couponType = couponType;
        this.legacyTicketId = legacyTicketId;
        this.sourceKey = sourceKey;
        this.merchantId = merchantId;
        this.minimumAmount = minimumAmount;
        this.discountAmount = discountAmount;
        this.expiresAt = expiresAt;
        this.status = CouponStatus.AVAILABLE;
    }

    public void reserve(String orderNo, Date reservationExpiresAt) {
        if (status != CouponStatus.AVAILABLE) {
            throw new IllegalStateException("coupon is not available");
        }
        status = CouponStatus.RESERVED;
        reservedOrderNo = orderNo;
        this.reservationExpiresAt = reservationExpiresAt;
    }

    public void consume() {
        if (status != CouponStatus.RESERVED) {
            throw new IllegalStateException("coupon is not reserved");
        }
        status = CouponStatus.USED;
        reservationExpiresAt = null;
    }

    public void release(Date now) {
        if (status == CouponStatus.RESERVED) {
            status = expiresAt.before(now) ? CouponStatus.EXPIRED : CouponStatus.AVAILABLE;
            reservedOrderNo = null;
            reservationExpiresAt = null;
        }
    }

    public void restoreAfterRefund(Date now) {
        if (status != CouponStatus.USED) {
            return;
        }
        status = expiresAt.before(now) ? CouponStatus.EXPIRED : CouponStatus.AVAILABLE;
        reservedOrderNo = null;
        reservationExpiresAt = null;
    }

    public void expire() { status = CouponStatus.EXPIRED; }
    public String getCouponNo() { return couponNo; }
    public Long getBuyerId() { return buyerId; }
    public CouponType getCouponType() { return couponType; }
    public String getSourceKey() { return sourceKey; }
    public Long getMerchantId() { return merchantId; }
    public BigDecimal getMinimumAmount() { return minimumAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public Date getExpiresAt() { return expiresAt; }
    public CouponStatus getStatus() { return status; }
    public String getReservedOrderNo() { return reservedOrderNo; }
    public Date getReservationExpiresAt() { return reservationExpiresAt; }
}
