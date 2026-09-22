package com.own.order.dto;

import com.own.order.domain.OrderEvent;
import java.util.Date;

/** Safe customer-facing projection; operational Outbox fields stay internal. */
public final class OrderTimelineEvent {
    private final String subOrderNo;
    private final String afterSaleNo;
    private final String eventType;
    private final Date occurredAt;

    private OrderTimelineEvent(OrderEvent event) {
        this.subOrderNo = event.getSubOrderNo();
        this.afterSaleNo = event.getAfterSaleNo();
        this.eventType = event.getEventType();
        this.occurredAt = event.getCreatedAt() == null ? null : new Date(event.getCreatedAt().getTime());
    }

    public static OrderTimelineEvent from(OrderEvent event) { return event == null ? null : new OrderTimelineEvent(event); }
    public String getSubOrderNo() { return subOrderNo; }
    public String getAfterSaleNo() { return afterSaleNo; }
    public String getEventType() { return eventType; }
    public Date getOccurredAt() { return occurredAt == null ? null : new Date(occurredAt.getTime()); }
}
