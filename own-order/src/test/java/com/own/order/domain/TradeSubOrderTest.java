package com.own.order.domain;

import static org.junit.Assert.assertEquals;
import java.math.BigDecimal;
import org.junit.Test;

public class TradeSubOrderTest {
    @Test
    public void shipmentCanBeCorrectedOnlyBeforeReceipt() {
        TradeSubOrder order = new TradeSubOrder("SUB-1", "ORD-1", 1L, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO);
        order.markToShip(); order.ship("old", "OLD-1"); order.correctShipment("new", "NEW-1");
        assertEquals("new", order.getLogisticsCompany()); assertEquals("NEW-1", order.getTrackingNo());
    }
}
