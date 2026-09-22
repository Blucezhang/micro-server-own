package com.own.settlement.domain;

import com.own.face.trade.TradeException;

/** Locally simulated payment channels. Neither value invokes a real provider. */
public enum PaymentChannel {
    MOCK_WECHAT, MOCK_ALIPAY;
    public static PaymentChannel parse(String value) {
        try { return value == null || value.trim().isEmpty() ? MOCK_WECHAT : valueOf(value.trim().toUpperCase()); }
        catch (IllegalArgumentException exception) { throw TradeException.unprocessable("channel must be MOCK_WECHAT or MOCK_ALIPAY"); }
    }
}
