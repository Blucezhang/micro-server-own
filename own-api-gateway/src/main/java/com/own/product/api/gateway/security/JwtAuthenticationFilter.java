package com.own.product.api.gateway.security;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.own.face.security.JwtPrincipal;
import com.own.face.security.JwtTokenService;
import com.own.face.trade.TradeHeaders;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** API edge filter: external actor headers are replaced by verified JWT claims. */
@Component
public class JwtAuthenticationFilter extends ZuulFilter {
    private final JwtTokenService tokenService;
    private final boolean enabled;

    public JwtAuthenticationFilter(JwtTokenService tokenService,
                                   @Value("${trade.security.jwt.enabled:false}") boolean enabled) {
        this.tokenService = tokenService;
        this.enabled = enabled;
    }
    @Override public String filterType() { return "pre"; }
    @Override public int filterOrder() { return -10; }
    @Override public boolean shouldFilter() {
        return enabled && !isPublic(RequestContext.getCurrentContext().getRequest());
    }
    @Override public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        try {
            String header = context.getRequest().getHeader("Authorization");
            if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7) || header.length() <= 7) {
                reject(context, "bearer token is required"); return null;
            }
            context.setRequest(new AuthenticatedRequest(context.getRequest(), tokenService.verify(header.substring(7).trim())));
        } catch (RuntimeException exception) { reject(context, "invalid or expired bearer token"); }
        return null;
    }
    private static boolean isPublic(String path) {
        String value = path == null ? "" : path.toLowerCase();
        return value.equals("/user/login/login") || value.equals("/user/login/token") || value.equals("/user/login/refresh") || value.equals("/user/login/logout") || value.equals("/user/login/updatepassword") || value.equals("/health")
                || value.equals("/actuator/health") || value.startsWith("/swagger") || value.startsWith("/v2/api-docs");
    }
    static boolean isPublic(HttpServletRequest request) {
        if (request == null) return false;
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/user/api/v1/auth/registrations".equals(request.getRequestURI())) return true;
        if ("POST".equalsIgnoreCase(request.getMethod()) && request.getRequestURI() != null
                && request.getRequestURI().toLowerCase().startsWith("/settlement/api/v1/payments/mock-callbacks/")) return true;
        // Only the explicit storefront list and public-review list are anonymous.
        // Do not use a prefix here: a later GET endpoint below /products must opt in.
        if ("GET".equalsIgnoreCase(request.getMethod()) && request.getRequestURI() != null) {
            String value = request.getRequestURI().toLowerCase();
            if (value.matches("^/product/api/v1/products/?$")
                    || value.matches("^/product/api/v1/products/[0-9]+/?$")
                    || value.matches("^/product/api/v1/products/[0-9]+/reviews/?$")
                    || value.matches("^/product/api/v1/categories/?$")) return true;
        }
        return isPublic(request.getRequestURI());
    }
    private void reject(RequestContext context, String message) {
        context.setSendZuulResponse(false); context.setResponseStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
        context.getResponse().setContentType("application/json;charset=UTF-8");
        try { context.getResponse().getWriter().write("{\"status\":401,\"message\":\"" + message + "\"}"); } catch (IOException ignored) { }
    }
    private static final class AuthenticatedRequest extends HttpServletRequestWrapper {
        private final JwtPrincipal principal;
        private AuthenticatedRequest(HttpServletRequest request, JwtPrincipal principal) { super(request); this.principal = principal; }
        @Override public String getHeader(String name) {
            if (TradeHeaders.ACTOR_ID.equalsIgnoreCase(name)) return String.valueOf(principal.getActorId());
            if (TradeHeaders.ACTOR_TYPE.equalsIgnoreCase(name)) return principal.getActorType().name();
            if ("X-User-Id".equalsIgnoreCase(name)) return String.valueOf(principal.getUserId());
            if ("X-Role-Names".equalsIgnoreCase(name)) return join(principal.getRoles());
            if ("X-Permission-Names".equalsIgnoreCase(name)) return join(principal.getPermissions());
            return super.getHeader(name);
        }
        @Override public Enumeration<String> getHeaders(String name) {
            if (isReplaced(name)) return Collections.enumeration(Collections.singletonList(getHeader(name)));
            return super.getHeaders(name);
        }
        @Override public Enumeration<String> getHeaderNames() {
            Set<String> names = new LinkedHashSet<String>(); Enumeration<String> existing = super.getHeaderNames();
            while (existing != null && existing.hasMoreElements()) { String name = existing.nextElement(); if (!isReplaced(name)) names.add(name); }
            names.add(TradeHeaders.ACTOR_ID); names.add(TradeHeaders.ACTOR_TYPE); names.add("X-User-Id"); names.add("X-Role-Names"); names.add("X-Permission-Names");
            return Collections.enumeration(new ArrayList<String>(names));
        }
        private boolean isReplaced(String name) { return TradeHeaders.ACTOR_ID.equalsIgnoreCase(name) || TradeHeaders.ACTOR_TYPE.equalsIgnoreCase(name) || "X-User-Id".equalsIgnoreCase(name) || "X-Role-Names".equalsIgnoreCase(name) || "X-Permission-Names".equalsIgnoreCase(name); }
        private String join(List<String> values) { return values == null ? "" : String.join(",", values); }
    }
}
