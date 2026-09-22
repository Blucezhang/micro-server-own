package com.own.order.repository;

import com.own.order.domain.OrderItem;
import com.own.order.domain.SubOrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderNo(String orderNo);
    List<OrderItem> findBySubOrderNo(String subOrderNo);
    @Query("select count(i) from OrderItem i, TradeSubOrder s, TradeOrder o where i.subOrderNo = s.subOrderNo and s.orderNo = o.orderNo and o.buyerId = ?1 and i.productId = ?2 and s.status = ?3")
    long countByBuyerProductAndSubOrderStatus(Long buyerId, Long productId, SubOrderStatus status);
}
