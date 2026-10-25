package com.own.face.trade.inbox;

import java.security.MessageDigest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.own.face.trade.TradeException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(name = "trade.inbox.consumer-name")
public class TradeMessageInboxService {
    private final TradeMessageInboxRepository repository;
    private final ObjectMapper objectMapper;
    public TradeMessageInboxService(TradeMessageInboxRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper.copy().enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    }

    @Transactional
    public boolean consume(String consumer, Object messageId, Object payload, Runnable businessAction) {
        if (consumer == null || consumer.isBlank() || consumer.length() > 64 || messageId == null
                || String.valueOf(messageId).isBlank() || String.valueOf(messageId).length() > 128 || businessAction == null) {
            throw new IllegalArgumentException("bounded consumer, eventId and business action are required");
        }
        String id = String.valueOf(messageId), hash = sha256(payload);
        TradeMessageInbox existing = repository.findByConsumerNameAndMessageId(consumer, id);
        if (existing != null) {
            if (!hash.equals(existing.getPayloadHash())) throw TradeException.conflict("eventId was reused with different payload");
            return false;
        }
        // A competing unique insert fails for redelivery; unrelated SQL errors must not be swallowed.
        repository.insertMessage(consumer, id, hash);
        businessAction.run();
        return true;
    }

    private String sha256(Object value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(objectMapper.writeValueAsBytes(value));
            StringBuilder result = new StringBuilder(64);
            for (byte item : bytes) result.append(String.format("%02x", item & 0xff));
            return result.toString();
        } catch (Exception exception) { throw new IllegalStateException("SHA-256 is unavailable", exception); }
    }
}
