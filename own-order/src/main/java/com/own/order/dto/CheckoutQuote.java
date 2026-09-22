package com.own.order.dto;

import java.math.BigDecimal;
import java.util.Map;

public class CheckoutQuote {
    private final BigDecimal totalAmount;
    private final BigDecimal discountAmount;
    private final BigDecimal payableAmount;
    private final Map<Long, BigDecimal> merchantTotals;
    private final Map<Long, BigDecimal> merchantDiscounts;
    private final Map<Long, BigDecimal> merchantFreights;

    public CheckoutQuote(BigDecimal totalAmount, BigDecimal discountAmount, Map<Long, BigDecimal> merchantTotals,
                         Map<Long, BigDecimal> merchantDiscounts, Map<Long, BigDecimal> merchantFreights) {
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
        BigDecimal freight = BigDecimal.ZERO;
        for (BigDecimal value : merchantFreights.values()) freight = freight.add(value);
        this.payableAmount = totalAmount.subtract(discountAmount).add(freight);
        this.merchantTotals = merchantTotals;
        this.merchantDiscounts = merchantDiscounts;
        this.merchantFreights = merchantFreights;
    }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getPayableAmount() { return payableAmount; }
    public Map<Long, BigDecimal> getMerchantTotals() { return merchantTotals; }
    public Map<Long, BigDecimal> getMerchantDiscounts() { return merchantDiscounts; }
    public Map<Long, BigDecimal> getMerchantFreights() { return merchantFreights; }
}
