package com.own.settlement.repository;
import com.own.settlement.domain.MerchantWithdrawal;
import java.util.List;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
public interface MerchantWithdrawalRepository extends JpaRepository<MerchantWithdrawal, Long> {
    MerchantWithdrawal findByWithdrawalNo(String withdrawalNo);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select w from MerchantWithdrawal w where w.merchantId = ?1 and w.status in ('REQUESTED','PAID')")
    List<MerchantWithdrawal> lockReservedByMerchantId(Long merchantId);
}
