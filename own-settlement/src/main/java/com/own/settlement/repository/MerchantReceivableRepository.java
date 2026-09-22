package com.own.settlement.repository;

import com.own.settlement.domain.MerchantReceivable;
import java.util.Date;
import java.util.List;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface MerchantReceivableRepository extends JpaRepository<MerchantReceivable, Long> {
    MerchantReceivable findByOrderNoAndMerchantId(String orderNo, Long merchantId);
    List<MerchantReceivable> findByStatusAndCreatedAtBefore(String status, Date cutoff);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from MerchantReceivable r where r.merchantId = ?1 and r.status = 'SETTLED'")
    List<MerchantReceivable> lockSettledByMerchantId(Long merchantId);
    List<MerchantReceivable> findByOrderNo(String orderNo);
}
