package com.own.order.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class TradeSubOrderTest {
    @Test
    public void shipmentCanBeCorrectedOnlyBeforeReceipt() {
        TradeSubOrder order = new TradeSubOrder("SUB-1", "ORD-1", 1L, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO);
        order.markToShip(); order.ship("old", "OLD-1"); order.correctShipment("new", "NEW-1");
        assertEquals("new", order.getLogisticsCompany()); assertEquals("NEW-1", order.getTrackingNo());
    }

    @Test
    public void asynchronousCheckoutCannotBeShippedBeforeTheSagaIsReady() {
        TradeSubOrder order = new TradeSubOrder("SUB-1", "ORD-1", 1L, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO);
        order.startSaga();
        assertEquals(SubOrderStatus.PROCESSING, order.getStatus());
        order.sagaReadyForPayment();
        assertEquals(SubOrderStatus.PENDING_PAYMENT, order.getStatus());
    }
}
