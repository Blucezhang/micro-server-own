package com.own.face.trade.inbox;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeMessageInboxService {
    private final TradeMessageInboxRepository repository;
    public TradeMessageInboxService(TradeMessageInboxRepository repository) { this.repository = repository; }

    @Transactional
    public boolean accept(String consumer, Object messageId, Object payload) {
        if (messageId == null || String.valueOf(messageId).trim().isEmpty()) {
            throw new IllegalArgumentException("eventId is required for Inbox deduplication");
        }
        return repository.insertIfAbsent(consumer, String.valueOf(messageId), sha256(String.valueOf(payload))) == 1;
    }

    private String sha256(String value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte item : bytes) result.append(String.format("%02x", item & 0xff));
            return result.toString();
        } catch (Exception exception) { throw new IllegalStateException("SHA-256 is unavailable", exception); }
    }
}
