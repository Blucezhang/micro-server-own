package com.own.product.api.gateway.security;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.own.face.security.JwtPrincipal;
import com.own.face.security.JwtTokenService;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Enforces role/permission policy only after the authentication filter verified a JWT. */
@Component
public class RbacAuthorizationFilter extends ZuulFilter {
    private final JwtTokenService tokenService;
    private final boolean enabled;
    private final RbacAccessPolicy policy = new RbacAccessPolicy();

    public RbacAuthorizationFilter(JwtTokenService tokenService,
                                   @Value("${trade.security.jwt.enabled:false}") boolean enabled) {
        this.tokenService = tokenService;
        this.enabled = enabled;
    }

    @Override public String filterType() { return "pre"; }
    @Override public int filterOrder() { return -9; }
    @Override public boolean shouldFilter() {
        RequestContext context = RequestContext.getCurrentContext();
        return enabled && context.sendZuulResponse() && !JwtAuthenticationFilter.isPublic(context.getRequest());
    }
    @Override public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        try {
            String header = context.getRequest().getHeader("Authorization");
            JwtPrincipal principal = tokenService.verify(header.substring(7).trim());
            if (!policy.decide(context.getRequest().getRequestURI(), context.getRequest().getMethod(), principal).isAllowed()) reject(context);
        } catch (RuntimeException exception) {
            reject(context);
        }
        return null;
    }
    private void reject(RequestContext context) {
        context.setSendZuulResponse(false); context.setResponseStatusCode(HttpServletResponse.SC_FORBIDDEN);
        context.getResponse().setContentType("application/json;charset=UTF-8");
        try { context.getResponse().getWriter().write("{\"status\":403,\"message\":\"insufficient role or permission\"}"); } catch (IOException ignored) { }
    }
}
