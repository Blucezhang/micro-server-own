package com.own.order.service;

import com.own.order.domain.OrderEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/** Publishes only persisted Outbox records; the scheduler retries any broker failure. */
@Component
@Primary
@ConditionalOnProperty(name = "trade.outbox.rocketmq.enabled", havingValue = "true")
public class RocketMqOrderEventSink implements OrderEventSink {
    private final StreamBridge streamBridge;
    private final String bindingName;

    public RocketMqOrderEventSink(StreamBridge streamBridge,
                                  @Value("${trade.outbox.rocketmq.binding:orderEvents-out-0}") String bindingName) {
        this.streamBridge = streamBridge;
        this.bindingName = bindingName;
    }

    @Override public boolean isEnabled() { return true; }
    @Override public String target() { return "ROCKETMQ"; }

    @Override
    public void deliver(OrderEvent event) {
        Map<String, Object> message = new LinkedHashMap<String, Object>();
        message.put("eventId", event.getId());
        message.put("orderNo", event.getOrderNo());
        message.put("subOrderNo", event.getSubOrderNo());
        message.put("afterSaleNo", event.getAfterSaleNo());
        message.put("eventType", event.getEventType());
        message.put("actorId", event.getActorId());
        message.put("actorType", event.getActorType());
        message.put("payload", event.getPayload());
        message.put("occurredAt", event.getCreatedAt());
        if (!streamBridge.send(bindingName, message)) {
            throw new IllegalStateException("RocketMQ binder rejected order event " + event.getId());
        }
    }
}
