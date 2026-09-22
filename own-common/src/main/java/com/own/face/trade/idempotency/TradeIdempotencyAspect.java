package com.own.face.trade.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.face.trade.TradeHeaders;
import com.own.face.util.Resp;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnProperty(name = "trade.idempotency.enabled", havingValue = "true")
public class TradeIdempotencyAspect {
    private final TradeIdempotencyStore store;
    private final ObjectMapper objectMapper;
    private final String serviceName;
    private final int retentionHours;

    public TradeIdempotencyAspect(TradeIdempotencyStore store, ObjectMapper objectMapper,
                                  @Value("${spring.application.name}") String serviceName,
                                  @Value("${trade.idempotency.retention-hours:24}") int retentionHours) {
        this.store = store; this.objectMapper = objectMapper;
        this.serviceName = serviceName; this.retentionHours = retentionHours;
    }

    @Around("@within(org.springframework.web.bind.annotation.RestController) && execution(public * *(..))")
    public Object protect(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = request(joinPoint.getArgs());
        if (!isTradeWrite(request)) return joinPoint.proceed();
        // A registration has no authenticated principal yet. Scope its idempotency
        // record to a reserved anonymous-buyer actor and never trust caller headers.
        TradeActor actor = isAnonymousBuyerRegistration(request)
                ? new TradeActor(0L, com.own.face.trade.ActorType.BUYER)
                : TradeHeaders.actor(request);
        String key = TradeHeaders.idempotencyKey(request);
        String path = request.getRequestURI();
        String hash = requestHash(joinPoint.getArgs());
        IdempotencyRecord record = store.begin(serviceName, actor, path, key, hash, retentionHours);
        if ("COMPLETED".equals(record.getStatus())) return objectMapper.readValue(record.getResponseBody(), Resp.class);
        try {
            Object result = joinPoint.proceed();
            if (!(result instanceof Resp)) throw new IllegalStateException("trade write must return Resp");
            store.complete(record.getId(), (Resp) result);
            return result;
        } catch (Throwable throwable) {
            store.abandon(record.getId());
            throw throwable;
        }
    }

    static boolean isTradeWrite(HttpServletRequest request) {
        if (request == null || request.getRequestURI() == null || !request.getRequestURI().contains("/api/v1")) return false;
        String method = request.getMethod();
        if (!("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method) || "DELETE".equals(method))) return false;
        String path = request.getRequestURI();
        // These POST endpoints calculate/verify only. Caching them as a write would
        // return stale quotes or require actor headers from a simulated provider.
        return !(path.endsWith("/checkouts/quote") || path.endsWith("/coupons/internal/quotes")
                || path.contains("/payments/mock-callbacks/"));
    }

    static boolean isAnonymousBuyerRegistration(HttpServletRequest request) {
        if (request == null || !"POST".equalsIgnoreCase(request.getMethod())) return false;
        // Zuul strips /user before forwarding; accept the external and service paths.
        String path = request.getRequestURI();
        return "/user/api/v1/auth/registrations".equals(path)
                || "/api/v1/auth/registrations".equals(path);
    }
    private HttpServletRequest request(Object[] args) {
        for (Object arg : args) if (arg instanceof HttpServletRequest) return (HttpServletRequest) arg;
        return null;
    }
    private String requestHash(Object[] args) {
        try {
            List<Object> material = new ArrayList<Object>();
            for (Object arg : args) if (!(arg instanceof HttpServletRequest)) material.add(arg);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(objectMapper.writeValueAsBytes(material));
            StringBuilder value = new StringBuilder();
            for (byte item : digest) value.append(String.format("%02x", item));
            return value.toString();
        } catch (Exception exception) { throw TradeException.badRequest("request cannot be fingerprinted for idempotency"); }
    }
}
