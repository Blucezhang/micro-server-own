package com.own.promotion.coupon.service;

import com.own.face.trade.inbox.OrderEventTypes;
import com.own.face.trade.inbox.TradeMessageInboxService;
import com.own.face.trade.inbox.TradeOrderEventMessage;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CouponOrderEventConsumer {
    @Bean
    public Consumer<Map<String, Object>> tradeOrderEventInbox(TradeMessageInboxService inbox, CouponService coupons,
            @Value("${trade.inbox.consumer-name}") String consumerName) {
        return event -> {
            TradeOrderEventMessage message = TradeOrderEventMessage.read(event);
            if (!OrderEventTypes.ORDER_CANCELED.equals(message.eventType()) && !OrderEventTypes.ORDER_EXPIRED.equals(message.eventType())) return;
            inbox.consume(consumerName, message.eventId(), event, () -> coupons.releaseOrder(message.orderNo()));
        };
    }
}
