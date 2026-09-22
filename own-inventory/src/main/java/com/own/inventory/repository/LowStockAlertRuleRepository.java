package com.own.inventory.repository;

import com.own.inventory.domain.LowStockAlertRule;
import java.util.List;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface LowStockAlertRuleRepository extends JpaRepository<LowStockAlertRule, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from LowStockAlertRule r where r.productId = ?1 and r.merchantId = ?2")
    LowStockAlertRule findByProductIdAndMerchantIdForUpdate(Long productId, Long merchantId);

    List<LowStockAlertRule> findByMerchantIdAndEnabledTrueOrderByIdAsc(Long merchantId);
    List<LowStockAlertRule> findByEnabledTrueOrderByIdAsc();
}
