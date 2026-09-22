package com.own.face.trade;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TradeCorrelationFilter extends OncePerRequestFilter {
    public static final String HEADER = TradeCorrelation.HEADER;
    public static final String ATTRIBUTE = TradeCorrelationFilter.class.getName() + ".value";
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String correlationId = TradeCorrelation.resolve(request == null ? null : request.getHeader(HEADER));
        request.setAttribute(ATTRIBUTE, correlationId);
        MDC.put("correlationId", correlationId);
        response.setHeader(HEADER, correlationId);
        try { chain.doFilter(request, response); }
        finally { MDC.remove("correlationId"); }
    }

}
