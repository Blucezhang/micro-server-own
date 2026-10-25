package com.own.face.trade.inbox;

import java.util.Map;

/** Required fields of the existing order Outbox envelope; the full map is fingerprinted by Inbox. */
public record TradeOrderEventMessage(String eventId, String orderNo, String eventType) {
    public static TradeOrderEventMessage read(Map<String, Object> event) {
        if (event == null || event.get("eventId") == null || !(event.get("orderNo") instanceof String orderNo)
                || orderNo.isBlank() || orderNo.length() > 64 || !(event.get("eventType") instanceof String type)
                || type.isBlank() || type.length() > 64) throw new IllegalArgumentException("invalid order event envelope");
        String id = String.valueOf(event.get("eventId"));
        if (id.isBlank() || id.length() > 128) throw new IllegalArgumentException("invalid order eventId");
        return new TradeOrderEventMessage(id, orderNo, type);
    }
}
