package com.own.product.audit.repository;

import com.own.product.audit.domain.ProductPriceAudit;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductPriceAuditRepository extends JpaRepository<ProductPriceAudit, Long> {
    Page<ProductPriceAudit> findByProductIdAndMerchantIdOrderByIdDesc(Long productId, Long merchantId, Pageable pageable);

    Optional<ProductPriceAudit> findByEventKey(String eventKey);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
            value = "INSERT INTO prd_product_price_audit ("
                    + "event_key, product_id, merchant_id, original_price_before, original_price_after, "
                    + "promotion_price_before, promotion_price_after, reason, changed_at"
                    + ") VALUES ("
                    + ":eventKey, :productId, :merchantId, :originalPriceBefore, :originalPriceAfter, "
                    + ":promotionPriceBefore, :promotionPriceAfter, :reason, :changedAt"
                    + ") ON DUPLICATE KEY UPDATE event_key = event_key",
            nativeQuery = true)
    int insertOnDuplicateKeyDoNothing(
            @Param("eventKey") String eventKey,
            @Param("productId") Long productId,
            @Param("merchantId") Long merchantId,
            @Param("originalPriceBefore") BigDecimal originalPriceBefore,
            @Param("originalPriceAfter") BigDecimal originalPriceAfter,
            @Param("promotionPriceBefore") BigDecimal promotionPriceBefore,
            @Param("promotionPriceAfter") BigDecimal promotionPriceAfter,
            @Param("reason") String reason,
            @Param("changedAt") Date changedAt);
}
