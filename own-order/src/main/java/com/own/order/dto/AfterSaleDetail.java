package com.own.order.dto;

import com.own.order.domain.AfterSaleEvidence;
import com.own.order.domain.AfterSaleItem;
import java.util.List;

/** Read model intentionally limited to one after-sale request and its own item lines. */
public class AfterSaleDetail {
    private final AfterSaleView afterSale;
    private final List<AfterSaleItem> items;
    private final List<OrderTimelineEvent> events;
    private final List<AfterSaleEvidence> evidence;

    public AfterSaleDetail(AfterSaleView afterSale, List<AfterSaleItem> items, List<OrderTimelineEvent> events,
                           List<AfterSaleEvidence> evidence) {
        this.afterSale = afterSale;
        this.items = items;
        this.events = events;
        this.evidence = evidence;
    }

    public AfterSaleView getAfterSale() { return afterSale; }
    public List<AfterSaleItem> getItems() { return items; }
    public List<OrderTimelineEvent> getEvents() { return events; }
    public List<AfterSaleEvidence> getEvidence() { return evidence; }
}
