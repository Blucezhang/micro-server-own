package com.own.order.dto;

import java.util.List;

public class CheckoutCommand {
    private List<Long> cartItemIds;
    private Long addressId;
    private String shippingAddress;
    private List<CouponUse> coupons;
    public List<Long> getCartItemIds() { return cartItemIds; }
    public void setCartItemIds(List<Long> cartItemIds) { this.cartItemIds = cartItemIds; }
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public List<CouponUse> getCoupons() { return coupons; }
    public void setCoupons(List<CouponUse> coupons) { this.coupons = coupons; }
}
