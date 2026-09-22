package com.own.face.security;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Optional service boundary for deployments where all public traffic enters
 * through the gateway. It prevents direct callers from forging edge-injected
 * actor headers; trusted peer calls use the same internal service token.
 */
@Component
public class ServiceBoundaryFilter extends OncePerRequestFilter {
    private final InternalServiceGuard internalServiceGuard;
    private final boolean enabled;

    public ServiceBoundaryFilter(InternalServiceGuard internalServiceGuard,
                                 @Value("${trade.security.service-boundary.enabled:false}") boolean enabled) {
        this.internalServiceGuard = internalServiceGuard;
        this.enabled = enabled;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled || request == null) return true;
        String path = request.getRequestURI();
        return "/health".equals(path) || "/actuator/health".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            internalServiceGuard.require(request);
        } catch (RuntimeException exception) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":403,\"message\":\"trusted gateway or internal service token is required\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
