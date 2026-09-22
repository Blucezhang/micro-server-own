package com.own.product.api.gateway.security;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.own.face.trade.TradeCorrelationFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.springframework.stereotype.Component;

/** Propagates the edge correlation ID into every routed service request. */
@Component
public class CorrelationHeaderInjectionFilter extends ZuulFilter {
    @Override public String filterType() { return "pre"; }
    @Override public int filterOrder() { return -12; }
    @Override public boolean shouldFilter() { return RequestContext.getCurrentContext().sendZuulResponse(); }
    @Override public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        Object stored = request.getAttribute(TradeCorrelationFilter.ATTRIBUTE);
        String correlationId = stored instanceof String ? (String) stored : TradeCorrelationFilter.resolve(request.getHeader(TradeCorrelationFilter.HEADER));
        context.setRequest(new CorrelatedRequest(request, correlationId));
        return null;
    }

    private static final class CorrelatedRequest extends HttpServletRequestWrapper {
        private final String correlationId;
        private CorrelatedRequest(HttpServletRequest request, String correlationId) { super(request); this.correlationId = correlationId; }
        @Override public String getHeader(String name) {
            if (TradeCorrelationFilter.HEADER.equalsIgnoreCase(name)) return correlationId;
            return super.getHeader(name);
        }
        @Override public Enumeration<String> getHeaders(String name) {
            if (TradeCorrelationFilter.HEADER.equalsIgnoreCase(name)) return Collections.enumeration(Collections.singletonList(correlationId));
            return super.getHeaders(name);
        }
        @Override public Enumeration<String> getHeaderNames() {
            Set<String> names = new LinkedHashSet<String>();
            Enumeration<String> existing = super.getHeaderNames();
            while (existing != null && existing.hasMoreElements()) {
                String name = existing.nextElement();
                if (!TradeCorrelationFilter.HEADER.equalsIgnoreCase(name)) names.add(name);
            }
            names.add(TradeCorrelationFilter.HEADER);
            return Collections.enumeration(new ArrayList<String>(names));
        }
    }
}
