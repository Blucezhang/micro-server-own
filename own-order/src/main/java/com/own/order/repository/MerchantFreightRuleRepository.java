package com.own.order.repository;
import com.own.order.domain.MerchantFreightRule;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MerchantFreightRuleRepository extends JpaRepository<MerchantFreightRule, Long> { MerchantFreightRule findByMerchantId(Long merchantId); }
