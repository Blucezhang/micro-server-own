package com.own.face.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class TradeCorrelationFilterTest {
    @Test
    public void propagatesSafeClientCorrelationIdToAttributeAndResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(); request.addHeader(TradeCorrelationFilter.HEADER, "checkout-42:retry");
        MockHttpServletResponse response = new MockHttpServletResponse();
        new TradeCorrelationFilter().doFilter(request, response, new MockFilterChain());
        assertEquals("checkout-42:retry", request.getAttribute(TradeCorrelationFilter.ATTRIBUTE));
        assertEquals("checkout-42:retry", response.getHeader(TradeCorrelationFilter.HEADER));
    }

    @Test
    public void replacesUnsafeCorrelationValueInsteadOfWritingItToLogsOrHeaders() {
        String generated = TradeCorrelation.resolve("bad\nvalue");
        assertNotEquals("bad\nvalue", generated);
        assertEquals(36, generated.length());
    }
}
