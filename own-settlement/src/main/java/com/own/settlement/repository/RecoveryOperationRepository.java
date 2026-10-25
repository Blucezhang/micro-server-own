package com.own.settlement.repository;

import com.own.settlement.domain.RecoveryOperation;
import com.own.face.trade.recovery.RecoveryStatus;
import jakarta.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface RecoveryOperationRepository extends JpaRepository<RecoveryOperation, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    RecoveryOperation findByOperationKey(String operationKey);
    @Query("select count(r) > 0 from RecoveryOperation r where r.paymentNo = ?1 and r.action = com.own.settlement.domain.RecoveryAction.PAYMENT and r.status <> '" + RecoveryStatus.COMPLETED + "'")
    boolean hasUnresolvedPayment(String paymentNo);
    List<RecoveryOperation> findTop50ByStatusAndNextAttemptAtBeforeOrderByIdAsc(String status, Date before);
    List<RecoveryOperation> findTop100ByStatusOrderByIdDesc(String status);
}
