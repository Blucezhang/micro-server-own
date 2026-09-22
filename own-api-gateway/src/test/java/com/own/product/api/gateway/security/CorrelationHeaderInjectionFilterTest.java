package com.own.product.api.gateway.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.own.face.trade.TradeCorrelation;
import org.junit.jupiter.api.Test;

/** Covers the correlation-id contract used by the Gateway WebFlux filter. */
public class CorrelationHeaderInjectionFilterTest {

    @Test
    public void preservesSafeClientCorrelationId() {
        assertEquals("gateway-value", TradeCorrelation.resolve("gateway-value"));
    }

    @Test
    public void replacesUnsafeClientCorrelationId() {
        String correlationId = TradeCorrelation.resolve("unsafe\nvalue");

        assertTrue(correlationId.matches("[A-Za-z0-9._:-]{1,100}"));
    }
}
