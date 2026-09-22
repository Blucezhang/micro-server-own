package com.own.face.trade.inbox;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "trade_message_inbox")
public class TradeMessageInbox {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "consumer_name", nullable = false, length = 64) private String consumerName;
    @Column(name = "message_id", nullable = false, length = 128) private String messageId;
    @Column(name = "payload_hash", nullable = false, length = 64) private String payloadHash;
    @Column(name = "processed_at", nullable = false) private Date processedAt;
}
