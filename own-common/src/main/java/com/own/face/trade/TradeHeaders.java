package com.own.face.trade;

import javax.servlet.http.HttpServletRequest;

public final class TradeHeaders {

    public static final String ACTOR_ID = "X-Actor-Id";
    public static final String ACTOR_TYPE = "X-Actor-Type";
    public static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private TradeHeaders() {
    }

    public static TradeActor actor(HttpServletRequest request) {
        String rawId = request.getHeader(ACTOR_ID);
        String rawType = request.getHeader(ACTOR_TYPE);
        if (rawId == null || rawType == null) {
            throw TradeException.badRequest("X-Actor-Id and X-Actor-Type headers are required");
        }
        try {
            Long id = Long.valueOf(rawId);
            if (id.longValue() <= 0L) {
                throw TradeException.badRequest("X-Actor-Id must be positive");
            }
            return new TradeActor(id, ActorType.valueOf(rawType.trim().toUpperCase()));
        } catch (NumberFormatException exception) {
            throw TradeException.badRequest("X-Actor-Id must be numeric");
        } catch (IllegalArgumentException exception) {
            throw TradeException.badRequest("X-Actor-Type must be BUYER, MERCHANT or SYSTEM");
        }
    }

    public static String idempotencyKey(HttpServletRequest request) {
        String key = request.getHeader(IDEMPOTENCY_KEY);
        if (key == null || key.trim().isEmpty() || key.length() > 100) {
            throw TradeException.badRequest("Idempotency-Key header is required and must be at most 100 characters");
        }
        return key.trim();
    }
}
