package com.own.order.service;

import com.own.order.domain.OrderEvent;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

@Component
@ConditionalOnMissingBean(OrderEventSink.class)
public class DisabledOrderEventSink implements OrderEventSink {
    public boolean isEnabled() { return false; }
    public String target() { return "DISABLED"; }
    public void deliver(OrderEvent event) { throw new IllegalStateException("outbox delivery is disabled"); }
}
