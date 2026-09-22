package com.own.settlement.repository;

import com.own.settlement.domain.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByPaymentNo(String paymentNo);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.paymentNo = ?1")
    Payment findByPaymentNoForUpdate(String paymentNo);
    Payment findByOrderNo(String orderNo);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.orderNo = ?1")
    Payment findByOrderNoForUpdate(String orderNo);
    Payment findByRequestKey(String requestKey);
}
