package com.own.face.trade;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TradeCorrelationFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Correlation-Id";
    public static final String ATTRIBUTE = TradeCorrelationFilter.class.getName() + ".value";
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String correlationId = resolve(request == null ? null : request.getHeader(HEADER));
        request.setAttribute(ATTRIBUTE, correlationId);
        MDC.put("correlationId", correlationId);
        response.setHeader(HEADER, correlationId);
        try { chain.doFilter(request, response); }
        finally { MDC.remove("correlationId"); }
    }

    /** Keeps correlation values safe for logs and downstream HTTP headers. */
    public static String resolve(String requested) {
        String value = requested == null ? "" : requested.trim();
        return value.matches("[A-Za-z0-9._:-]{1,100}") ? value : UUID.randomUUID().toString();
    }
}
