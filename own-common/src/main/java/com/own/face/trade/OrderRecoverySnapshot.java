package com.own.face.trade;

import java.math.BigDecimal;

/** Transport projection; own-order owns these facts. No address or contact fields. */
public record OrderRecoverySnapshot(String orderNo, Long buyerId, BigDecimal payableAmount,
                                    String status, String paymentRecoveryStatus) {
    public boolean paymentCompleted() {
        return java.util.Set.of("PAID", "FULFILLING", "COMPLETED", "REFUNDING", "REFUNDED").contains(status);
    }
    public boolean refundCompleted() { return "REFUNDED".equals(status); }
}
