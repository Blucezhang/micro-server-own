package com.own.order.service;

import com.own.order.domain.OrderEvent;
import com.own.order.repository.OrderEventRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderOutboxService {
    private final OrderEventRepository repository;
    public OrderOutboxService(OrderEventRepository repository) { this.repository = repository; }
    public List<OrderEvent> find(String orderNo, String status) {
        List<OrderEvent> source = orderNo == null || orderNo.trim().isEmpty()
                ? repository.findAll() : repository.findByOrderNoOrderByIdAsc(orderNo);
        if (status == null || status.trim().isEmpty()) return source;
        List<OrderEvent> result = new ArrayList<OrderEvent>();
        for (OrderEvent event : source) if (status.equals(event.getDeliveryStatus())) result.add(event);
        return result;
    }
}
