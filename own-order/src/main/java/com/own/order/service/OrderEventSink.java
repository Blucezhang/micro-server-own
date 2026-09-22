package com.own.order.service;

import com.own.order.domain.OrderEvent;

/** Adapter boundary for a future workflow or notification integration. */
public interface OrderEventSink {
    boolean isEnabled();
    String target();
    void deliver(OrderEvent event);
}
