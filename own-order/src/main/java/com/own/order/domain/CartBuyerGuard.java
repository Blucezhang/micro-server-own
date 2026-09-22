package com.own.order.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** One permanent row per buyer, used only to serialize cart mutations. */
@Entity
@Table(name = "ord_cart_buyer_guard")
public class CartBuyerGuard {
    @Id private Long buyerId;
    protected CartBuyerGuard() { }
    public Long getBuyerId() { return buyerId; }
}
