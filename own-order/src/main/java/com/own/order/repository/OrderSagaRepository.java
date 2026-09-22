package com.own.order.repository;

import com.own.order.domain.OrderSaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Date;
import java.util.List;

public interface OrderSagaRepository extends JpaRepository<OrderSaga, Long> {
    OrderSaga findByOrderNo(String orderNo);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    OrderSaga findLockedByOrderNo(String orderNo);
    List<OrderSaga> findByStatusAndNextAttemptAtBeforeOrderByIdAsc(com.own.order.domain.OrderSagaStatus status, Date now);
}
