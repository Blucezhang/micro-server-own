package com.own.order.repository;

import com.own.order.domain.TradeSubOrder;
import com.own.order.domain.SubOrderStatus;
import jakarta.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TradeSubOrderRepository extends JpaRepository<TradeSubOrder, Long> {
    TradeSubOrder findBySubOrderNo(String subOrderNo);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from TradeSubOrder s where s.subOrderNo = ?1")
    TradeSubOrder findBySubOrderNoForUpdate(String subOrderNo);
    List<TradeSubOrder> findByOrderNo(String orderNo);
    List<TradeSubOrder> findByMerchantIdOrderByIdDesc(Long merchantId);
    List<TradeSubOrder> findByStatusAndShippedAtBefore(SubOrderStatus status, Date shippedAt);
}
