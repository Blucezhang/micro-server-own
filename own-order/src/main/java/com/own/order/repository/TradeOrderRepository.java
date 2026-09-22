package com.own.order.repository;

import com.own.order.domain.TradeOrder;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;
import java.util.Date;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {
    TradeOrder findByOrderNo(String orderNo);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    TradeOrder findLockedByOrderNo(String orderNo);
    TradeOrder findByRequestKey(String requestKey);
    List<TradeOrder> findByBuyerIdOrderByIdDesc(Long buyerId);
    @Query("select o from TradeOrder o where o.buyerId = ?1 and (?2 is null or o.status = ?2) and (?3 is null or o.createdAt >= ?3) and (?4 is null or o.createdAt <= ?4) order by o.id desc")
    Page<TradeOrder> pageByBuyer(Long buyerId, com.own.order.domain.OrderStatus status, Date from, Date to, Pageable pageable);
    List<TradeOrder> findByStatusAndCreatedAtBefore(com.own.order.domain.OrderStatus status, Date createdAt);
}
