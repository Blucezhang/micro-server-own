package com.own.product.service;

import com.own.product.audit.domain.ProductPriceAudit;
import com.own.product.audit.repository.ProductPriceAuditRepository;
import com.own.product.domain.ProductPriceChangeEvent;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductPriceAuditSqlWriter {

    private final ProductPriceAuditRepository auditRepository;

    @Autowired
    public ProductPriceAuditSqlWriter(ProductPriceAuditRepository auditRepository) {
        this.auditRepository = Objects.requireNonNull(auditRepository, "auditRepository is required");
    }

    @Transactional(transactionManager = "transactionManager", propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void projectAudit(ProductPriceChangeEvent event) {
        Objects.requireNonNull(event, "event is required");
        if (!Boolean.TRUE.equals(event.getPriceChanged())) {
            return;
        }
        String eventKey = requireEventKey(event.getEventKey());
        BigDecimal originalBefore = new BigDecimal(event.getOriginalPriceBefore());
        BigDecimal originalAfter = new BigDecimal(event.getOriginalPriceAfter());
        BigDecimal promotionBefore = toBigDecimal(event.getPromotionPriceBefore());
        BigDecimal promotionAfter = toBigDecimal(event.getPromotionPriceAfter());
        String reason = event.getReason() == null ? "" : event.getReason();
        Long epochSecond = Objects.requireNonNull(event.getChangedAtEpochSecond(), "changedAtEpochSecond is required");
        Date changedAt = new Date(epochSecond * 1000L);

        auditRepository.insertOnDuplicateKeyDoNothing(
                eventKey,
                event.getProductId(),
                event.getMerchantId(),
                originalBefore,
                originalAfter,
                promotionBefore,
                promotionAfter,
                reason,
                changedAt);

        ProductPriceAudit stored = auditRepository.findByEventKey(eventKey)
                .orElseThrow(() -> new IllegalStateException("Price audit record missing after upsert for eventKey=" + eventKey));
        verifyImmutableFields(stored, event, originalBefore, originalAfter, promotionBefore, promotionAfter, reason, epochSecond);
    }

    private void verifyImmutableFields(
            ProductPriceAudit stored,
            ProductPriceChangeEvent event,
            BigDecimal originalBefore,
            BigDecimal originalAfter,
            BigDecimal promotionBefore,
            BigDecimal promotionAfter,
            String reason,
            Long expectedEpochSecond) {
        boolean matches = Objects.equals(stored.getEventKey(), event.getEventKey())
                && Objects.equals(stored.getProductId(), event.getProductId())
                && Objects.equals(stored.getMerchantId(), event.getMerchantId())
                && ProductUpdateGraphTransactionService.samePrice(stored.getOriginalPriceBefore(), originalBefore)
                && ProductUpdateGraphTransactionService.samePrice(stored.getOriginalPriceAfter(), originalAfter)
                && ProductUpdateGraphTransactionService.samePrice(stored.getPromotionPriceBefore(), promotionBefore)
                && ProductUpdateGraphTransactionService.samePrice(stored.getPromotionPriceAfter(), promotionAfter)
                && Objects.equals(stored.getReason() == null ? "" : stored.getReason(), reason)
                && stored.getChangedAt() != null
                && (stored.getChangedAt().getTime() / 1000L) == expectedEpochSecond.longValue();
        if (!matches) {
            throw new IllegalStateException("Immutable price audit mismatch for eventKey=" + event.getEventKey());
        }
    }

    private String requireEventKey(String eventKey) {
        if (eventKey == null || eventKey.trim().isEmpty()) {
            throw new IllegalArgumentException("eventKey is required for SQL price audit projection");
        }
        return eventKey;
    }

    private BigDecimal toBigDecimal(String value) {
        return value == null || value.trim().isEmpty() ? null : new BigDecimal(value);
    }
}
