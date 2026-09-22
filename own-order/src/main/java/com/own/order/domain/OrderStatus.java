package com.own.order.domain;

public enum OrderStatus {
    PENDING_PAYMENT,
    PAID,
    FULFILLING,
    COMPLETED,
    CANCELED,
    REFUNDING,
    REFUNDED
}
