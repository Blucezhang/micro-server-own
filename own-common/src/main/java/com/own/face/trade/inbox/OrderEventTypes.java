package com.own.face.trade.inbox;

/** Existing Outbox/Inbox wire names; changing one requires producer/consumer contract coordination. */
public final class OrderEventTypes {
    public static final String ORDER_CANCELED = "ORDER_CANCELED";
    public static final String ORDER_EXPIRED = "ORDER_EXPIRED";
    public static final String PAYMENT_SUCCEEDED = "PAYMENT_SUCCEEDED";
    public static final String REFUND_SUCCEEDED = "REFUND_SUCCEEDED";

    private OrderEventTypes() { }
}
