package com.own.order.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class TradeOrderTest {

    @Test
    public void followsPaidFulfillmentAndCompletionStateMachine() {
        TradeOrder order = new TradeOrder("ORD-1", 1L, new BigDecimal("100.00"), new BigDecimal("10.00"), "address", "key");
        order.markPaid();
        order.markFulfilling();
        order.markCompleted();
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertEquals(new BigDecimal("90.00"), order.getPayableAmount());
    }

    @Test
    public void cannotCompleteWithoutPayment() {
        TradeOrder order = new TradeOrder("ORD-1", 1L, BigDecimal.ONE, BigDecimal.ZERO, "address", "key");
        try {
            order.markCompleted();
            fail("expected invalid state transition");
        } catch (IllegalStateException expected) {
            assertEquals("invalid order state transition", expected.getMessage());
        }
    }
}
