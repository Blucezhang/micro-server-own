package com.own.order.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

class OrderSagaTest {
    @Test
    void advancesToCouponImmediatelyInsteadOfLeavingTheSagaUnscheduled() {
        OrderSaga saga = new OrderSaga("SAGA-1", "ORD-1", "{}");

        saga.advance(OrderSagaStep.RESERVE_COUPON);

        assertEquals(OrderSagaStatus.PROCESSING, saga.getStatus());
        assertEquals(OrderSagaStep.RESERVE_COUPON, saga.getCurrentStep());
        assertNotNull(saga.getNextAttemptAt());
    }

    @Test
    void compensationCanBeRetriedWithoutMarkingTheOrderAsFailedEarly() {
        OrderSaga saga = new OrderSaga("SAGA-1", "ORD-1", "{}");
        saga.compensate("inventory unavailable", OrderSagaStep.COMPENSATE_INVENTORY);
        saga.retry("inventory unavailable", 10);

        assertEquals(OrderSagaStatus.COMPENSATING, saga.getStatus());
        assertEquals(1, saga.getAttempts());
        assertNotNull(saga.getNextAttemptAt());
        saga.fail();
        assertEquals(OrderSagaStatus.FAILED, saga.getStatus());
    }
}
