package com.own.order.domain;

public enum OrderStatus {
    PROCESSING,
    PENDING_PAYMENT,
    PAID,
    FULFILLING,
    COMPLETED,
    CANCELED,
    REFUNDING,
    REFUNDED,
    SAGA_FAILED
}
