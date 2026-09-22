package com.own.settlement.dto;

public class CreatePaymentCommand {
    private String orderNo;
    private String channel;
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
}
