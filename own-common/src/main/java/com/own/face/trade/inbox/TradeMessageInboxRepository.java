package com.own.face.trade.inbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TradeMessageInboxRepository extends JpaRepository<TradeMessageInbox, Long> {
    @Modifying
    @Query(value = "INSERT IGNORE INTO trade_message_inbox (consumer_name, message_id, payload_hash, processed_at) VALUES (:consumer, :messageId, :payloadHash, NOW())", nativeQuery = true)
    int insertIfAbsent(@Param("consumer") String consumer, @Param("messageId") String messageId,
                       @Param("payloadHash") String payloadHash);
}
