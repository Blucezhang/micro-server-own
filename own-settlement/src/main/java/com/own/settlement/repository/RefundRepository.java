package com.own.settlement.repository;

import com.own.settlement.domain.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
    Refund findByOrderNo(String orderNo);
}
