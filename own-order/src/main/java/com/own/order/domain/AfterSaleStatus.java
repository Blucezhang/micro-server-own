package com.own.order.domain;

import java.util.List;

public enum AfterSaleStatus {
    APPLYING, APPROVED, REJECTED, RETURNING, RECEIVED, EXCHANGE_PENDING_SHIPMENT,
    EXCHANGE_SHIPPED, EXCHANGED, REFUND_PENDING, REFUNDED, CLOSED;

    private static final List<AfterSaleStatus> ACTIVE = List.of(
            APPLYING, APPROVED, RETURNING, RECEIVED, EXCHANGE_PENDING_SHIPMENT,
            EXCHANGE_SHIPPED, REFUND_PENDING);

    public static List<AfterSaleStatus> activeStatuses() {
        return ACTIVE;
    }
}
