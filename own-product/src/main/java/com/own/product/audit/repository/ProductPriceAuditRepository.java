package com.own.product.audit.repository;

import com.own.product.audit.domain.ProductPriceAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductPriceAuditRepository extends JpaRepository<ProductPriceAudit, Long> {
    Page<ProductPriceAudit> findByProductIdAndMerchantIdOrderByIdDesc(Long productId, Long merchantId, Pageable pageable);
}
