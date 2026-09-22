package com.own.order.repository;

import com.own.order.domain.OrderEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Date;

public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {
    List<OrderEvent> findByOrderNoOrderByIdAsc(String orderNo);
    List<OrderEvent> findBySubOrderNoOrderByIdAsc(String subOrderNo);
    List<OrderEvent> findByAfterSaleNoOrderByIdAsc(String afterSaleNo);
    List<OrderEvent> findByDeliveryStatusAndNextAttemptAtBeforeOrderByIdAsc(String status, Date time);
    long countByDeliveryStatus(String status);
}
