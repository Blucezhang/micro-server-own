package com.own.product.api.gateway.security;

import com.own.face.security.InternalServiceGuard;
import com.own.face.security.JwtPrincipal;
import com.own.face.security.JwtTokenService;
import com.own.face.trade.TradeCorrelation;
import com.own.face.trade.TradeHeaders;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** Rebuilds trusted identity and service headers before a Gateway route is forwarded. */
@Component
public class GatewaySecurityFilter implements GlobalFilter, Ordered {
    private static final int MINIMUM_TOKEN_BYTES = 32;
    private final JwtTokenService tokenService;
    private final boolean jwtEnabled;
    private final String internalToken;
    private final RbacAccessPolicy policy = new RbacAccessPolicy();

    public GatewaySecurityFilter(JwtTokenService tokenService,
                                 @Value("${trade.security.jwt.enabled:false}") boolean jwtEnabled,
                                 @Value("${trade.internal.service-token:}") String internalToken) {
        this.tokenService = tokenService;
        this.jwtEnabled = jwtEnabled;
        this.internalToken = internalToken == null ? "" : internalToken;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = TradeCorrelation.resolve(request.getHeaders().getFirst(TradeCorrelation.HEADER));
        exchange.getResponse().getHeaders().set(TradeCorrelation.HEADER, correlationId);
        ServerHttpRequest.Builder builder = request.mutate().headers(headers -> applyTrustedHeaders(headers, correlationId));
        if (!jwtEnabled || isPublic(request)) return chain.filter(exchange.mutate().request(builder.build()).build());
        JwtPrincipal principal;
        try { principal = verify(request); }
        catch (RuntimeException exception) { return reject(exchange, HttpStatus.UNAUTHORIZED, "invalid or expired bearer token"); }
        if (!policy.decide(request.getPath().value(), request.getMethod().name(), principal).isAllowed()) {
            return reject(exchange, HttpStatus.FORBIDDEN, "insufficient role or permission");
        }
        builder.headers(headers -> applyPrincipal(headers, principal));
        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    private void applyTrustedHeaders(org.springframework.http.HttpHeaders headers, String correlationId) {
        headers.set(TradeCorrelation.HEADER, correlationId);
        headers.remove(InternalServiceGuard.HEADER);
        if (internalToken.getBytes(StandardCharsets.UTF_8).length >= MINIMUM_TOKEN_BYTES) headers.set(InternalServiceGuard.HEADER, internalToken);
    }

    private JwtPrincipal verify(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7) || header.length() <= 7) {
            throw new IllegalArgumentException("bearer token is required");
        }
        return tokenService.verify(header.substring(7).trim());
    }

    private void applyPrincipal(org.springframework.http.HttpHeaders headers, JwtPrincipal principal) {
        headers.set(TradeHeaders.ACTOR_ID, String.valueOf(principal.getActorId()));
        headers.set(TradeHeaders.ACTOR_TYPE, principal.getActorType().name());
        headers.set("X-User-Id", String.valueOf(principal.getUserId()));
        headers.set("X-Role-Names", String.join(",", principal.getRoles()));
        headers.set("X-Permission-Names", String.join(",", principal.getPermissions()));
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().set("Content-Type", "application/json;charset=UTF-8");
        byte[] body = ("{\"status\":" + status.value() + ",\"message\":\"" + message + "\"}").getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    static boolean isPublic(ServerHttpRequest request) {
        String path = request.getPath().value().toLowerCase();
        String method = request.getMethod().name();
        if ("POST".equals(method) && "/user/api/v1/auth/registrations".equals(path)) return true;
        if ("POST".equals(method) && path.startsWith("/settlement/api/v1/payments/mock-callbacks/")) return true;
        if ("GET".equals(method) && path.matches("^/product/api/v1/(products/?|products/[0-9]+/?|products/[0-9]+/reviews/?|categories/?)$")) return true;
        return path.equals("/user/login/login") || path.equals("/user/login/token") || path.equals("/user/login/refresh")
                || path.equals("/user/login/logout") || path.equals("/user/login/updatepassword") || path.equals("/health")
                || path.equals("/actuator/health") || path.startsWith("/swagger") || path.startsWith("/v3/api-docs");
    }

    @Override public int getOrder() { return -100; }
}
