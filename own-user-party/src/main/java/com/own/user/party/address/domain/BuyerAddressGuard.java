package com.own.user.party.address.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/** One permanent row per buyer, used only to serialize default-address updates. */
@Entity
@Table(name = "buyer_address_guard")
public class BuyerAddressGuard {
    @Id private Long buyerId;
    protected BuyerAddressGuard() { }
    public Long getBuyerId() { return buyerId; }
}
