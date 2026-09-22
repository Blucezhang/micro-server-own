package com.own.product.api.gateway.security;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.own.face.security.InternalServiceGuard;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Replaces any caller-supplied service token with the gateway's trusted token. */
@Component
public class InternalServiceTokenInjectionFilter extends ZuulFilter {
    private static final int MINIMUM_TOKEN_BYTES = 32;
    private final String token;

    public InternalServiceTokenInjectionFilter(@Value("${trade.internal.service-token:}") String token) {
        this.token = token == null ? "" : token;
    }

    @Override public String filterType() { return "pre"; }
    @Override public int filterOrder() { return -11; }
    @Override public boolean shouldFilter() {
        return RequestContext.getCurrentContext().sendZuulResponse()
                && token.getBytes(StandardCharsets.UTF_8).length >= MINIMUM_TOKEN_BYTES;
    }
    @Override public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        context.setRequest(new TokenRequest(context.getRequest(), token));
        return null;
    }

    private static final class TokenRequest extends HttpServletRequestWrapper {
        private final String token;
        private TokenRequest(HttpServletRequest request, String token) { super(request); this.token = token; }
        @Override public String getHeader(String name) {
            if (InternalServiceGuard.HEADER.equalsIgnoreCase(name)) return token;
            return super.getHeader(name);
        }
        @Override public Enumeration<String> getHeaders(String name) {
            if (InternalServiceGuard.HEADER.equalsIgnoreCase(name)) return Collections.enumeration(Collections.singletonList(token));
            return super.getHeaders(name);
        }
        @Override public Enumeration<String> getHeaderNames() {
            Set<String> names = new LinkedHashSet<String>();
            Enumeration<String> existing = super.getHeaderNames();
            while (existing != null && existing.hasMoreElements()) {
                String name = existing.nextElement();
                if (!InternalServiceGuard.HEADER.equalsIgnoreCase(name)) names.add(name);
            }
            names.add(InternalServiceGuard.HEADER);
            return Collections.enumeration(new ArrayList<String>(names));
        }
    }
}
