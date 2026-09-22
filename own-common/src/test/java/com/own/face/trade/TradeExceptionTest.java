package com.own.face.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class TradeExceptionTest {
    @Test
    public void conflictCanExposeSafeLatestSnapshot() {
        TradeException exception = TradeException.conflict("PRODUCT_CHANGED", "latest-product");
        assertEquals(409, exception.getStatus());
        assertEquals("latest-product", exception.getData());
    }
}
