package com.own.order.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class OrderEventTest {
    @Test
    public void disabledOutboxStartsPendingAndRecordsRetry() {
        OrderEvent event = new OrderEvent("ORD-1", null, "ORDER_CREATED", 1L, "BUYER", "pending_payment");
        assertEquals("PENDING", event.getDeliveryStatus());
        assertEquals(Integer.valueOf(0), event.getDeliveryAttempts());
        event.recordDeliveryFailure("downstream unavailable", 60);
        assertEquals(Integer.valueOf(1), event.getDeliveryAttempts());
        assertEquals("PENDING", event.getDeliveryStatus());
        event.markDeliveryFailed("maximum retries reached");
        assertEquals("FAILED", event.getDeliveryStatus());
        assertNull(event.getNextAttemptAt());
    }
}
