package com.own.order.domain;

public enum SubOrderStatus {
    PROCESSING,
    PENDING_PAYMENT,
    TO_SHIP,
    SHIPPED,
    RECEIVED,
    CANCELED,
    REFUNDED
}
