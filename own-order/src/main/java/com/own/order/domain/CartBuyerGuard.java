package com.own.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** One permanent row per buyer, used only to serialize cart mutations. */
@Entity
@Table(name = "ord_cart_buyer_guard")
public class CartBuyerGuard {
    @Id private Long buyerId;
    protected CartBuyerGuard() { }
    public Long getBuyerId() { return buyerId; }
}
