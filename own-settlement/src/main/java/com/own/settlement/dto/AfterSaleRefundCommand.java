package com.own.settlement.dto;

import java.math.BigDecimal;

/** Internal command sent by own-order after the after-sale has reached refund pending. */
public class AfterSaleRefundCommand {
    private String afterSaleNo;
    private String orderNo;
    private Long buyerId;
    private BigDecimal amount;
    public String getAfterSaleNo() { return afterSaleNo; }
    public void setAfterSaleNo(String value) { afterSaleNo = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long value) { buyerId = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
}
