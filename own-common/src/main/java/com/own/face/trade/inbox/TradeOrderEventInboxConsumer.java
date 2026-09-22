package com.own.face.trade.inbox;

import java.util.Map;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TradeOrderEventInboxConsumer {
    @Bean
    public Consumer<Map<String, Object>> tradeOrderEventInbox(TradeMessageInboxService inbox,
            @Value("${trade.inbox.consumer-name}") String consumerName) {
        return event -> {
            if (!inbox.accept(consumerName, event.get("eventId"), event)) return;
            // Business services add their own explicit Saga handlers after this atomic deduplication gate.
        };
    }
}
