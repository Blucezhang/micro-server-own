package com.own.settlement.dto;

import java.math.BigDecimal;

/** Payload accepted only by the local mock WeChat/Alipay callback endpoint. */
public class MockPaymentCallbackCommand {
    private String paymentNo;
    private String providerPaymentNo;
    private BigDecimal amount;
    private String result;

    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }
    public String getProviderPaymentNo() { return providerPaymentNo; }
    public void setProviderPaymentNo(String providerPaymentNo) { this.providerPaymentNo = providerPaymentNo; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
}
