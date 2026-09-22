package com.own.order.dto;

import com.own.order.domain.OrderItem;
import com.own.order.domain.TradeOrder;
import com.own.order.domain.TradeSubOrder;
import java.util.List;

public class OrderDetail {
    private final TradeOrder order;
    private final List<TradeSubOrder> subOrders;
    private final List<OrderItem> items;
    private final List<OrderTimelineEvent> events;

    public OrderDetail(TradeOrder order, List<TradeSubOrder> subOrders, List<OrderItem> items,
                       List<OrderTimelineEvent> events) {
        this.order = order;
        this.subOrders = subOrders;
        this.items = items;
        this.events = events;
    }

    public TradeOrder getOrder() { return order; }
    public List<TradeSubOrder> getSubOrders() { return subOrders; }
    public List<OrderItem> getItems() { return items; }
    public List<OrderTimelineEvent> getEvents() { return events; }
}
