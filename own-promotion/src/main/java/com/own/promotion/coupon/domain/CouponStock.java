package com.own.promotion.coupon.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "mkt_coupon_stock")
public class CouponStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scope_key", nullable = false, unique = true, length = 100)
    private String scopeKey;

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;

    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @Version
    @Column(nullable = false)
    private Long version;

    protected CouponStock() {
    }

    public CouponStock(String scopeKey, Integer totalQuantity) {
        this.scopeKey = scopeKey;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
    }

    public void claim() {
        if (availableQuantity.intValue() <= 0) {
            throw new IllegalStateException("coupon stock is exhausted");
        }
        availableQuantity--;
    }

    public String getScopeKey() { return scopeKey; }
    public Integer getAvailableQuantity() { return availableQuantity; }
}
