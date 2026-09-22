package com.own.settlement.repository;

import com.own.settlement.domain.AfterSaleRefund;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AfterSaleRefundRepository extends JpaRepository<AfterSaleRefund, Long> {
    AfterSaleRefund findByAfterSaleNo(String afterSaleNo);
    @Query("select coalesce(sum(r.amount), 0) from AfterSaleRefund r where r.paymentNo = :paymentNo and r.status = 'SUCCEEDED'")
    BigDecimal succeededAmountByPaymentNo(@Param("paymentNo") String paymentNo);
}
