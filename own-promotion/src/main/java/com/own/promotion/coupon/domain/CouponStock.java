package com.own.promotion.coupon.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

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
