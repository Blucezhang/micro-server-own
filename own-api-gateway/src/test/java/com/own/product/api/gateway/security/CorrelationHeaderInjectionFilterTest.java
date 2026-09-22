package com.own.product.api.gateway.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.netflix.zuul.context.RequestContext;
import com.own.face.trade.TradeCorrelationFilter;
import java.util.Collections;
import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class CorrelationHeaderInjectionFilterTest {

    @After
    public void clearContext() {
        RequestContext.getCurrentContext().clear();
    }

    @Test
    public void gatewayForwardsTheCorrelationIdProducedByTheServletFilter() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/product/api/v1/products");
        request.addHeader(TradeCorrelationFilter.HEADER, "client-value");
        request.setAttribute(TradeCorrelationFilter.ATTRIBUTE, "gateway-value");
        RequestContext context = RequestContext.getCurrentContext();
        context.setRequest(request);

        new CorrelationHeaderInjectionFilter().run();

        assertEquals("gateway-value", context.getRequest().getHeader(TradeCorrelationFilter.HEADER));
        assertEquals(Collections.singletonList("gateway-value"),
                Collections.list(context.getRequest().getHeaders(TradeCorrelationFilter.HEADER)));
    }

    @Test
    public void gatewayReplacesUnsafeClientCorrelationHeaderBeforeForwarding() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/product/api/v1/products");
        request.addHeader(TradeCorrelationFilter.HEADER, "unsafe\nvalue");
        RequestContext context = RequestContext.getCurrentContext();
        context.setRequest(request);

        new CorrelationHeaderInjectionFilter().run();

        String forwarded = context.getRequest().getHeader(TradeCorrelationFilter.HEADER);
        assertNotNull(forwarded);
        assertTrue(forwarded.matches("[A-Za-z0-9._:-]{1,100}"));
        assertEquals(Collections.singletonList(forwarded),
                Collections.list(context.getRequest().getHeaders(TradeCorrelationFilter.HEADER)));
    }
}
