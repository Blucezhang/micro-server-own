package com.own.product.service;

import com.own.product.config.ProductPriceAuditProperties;
import com.own.product.dao.ProductPriceChangeEventDao;
import com.own.product.domain.ProductPriceChangeEvent;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProductPriceAuditProjector {

    private final ProductPriceAuditProperties properties;

    private final ProductPriceAuditSqlWriter sqlWriter;
    private final ProductUpdateGraphTransactionService graphTxService;
    private final ProductPriceChangeEventDao eventDao;

    @Autowired
    public ProductPriceAuditProjector(
            ProductPriceAuditSqlWriter sqlWriter,
            ProductUpdateGraphTransactionService graphTxService,
            ProductPriceChangeEventDao eventDao, ProductPriceAuditProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties are required");
        this.sqlWriter = Objects.requireNonNull(sqlWriter, "sqlWriter is required");
        this.graphTxService = Objects.requireNonNull(graphTxService, "graphTxService is required");
        this.eventDao = Objects.requireNonNull(eventDao, "eventDao is required");
    }

    public void projectSingleEvent(ProductPriceChangeEvent event) {
        Objects.requireNonNull(event, "event is required");
        if (Boolean.TRUE.equals(event.getPriceChanged())) {
            sqlWriter.projectAudit(event);
        }
        if (!Boolean.TRUE.equals(event.getProjected())) {
            graphTxService.markProjected(event.getEventKey());
            event.setProjected(true);
        }
    }

    @Scheduled(fixedDelayString = "${own.product.price-audit.recovery-delay-ms}")
    public int recoverPendingProjections() {
        return recoverPendingProjections(properties.batchSize());
    }

    public int recoverPendingProjections(int requestedBatchSize) {
        int batchSize = Math.min(Math.max(requestedBatchSize, 1), properties.batchSize());
        List<ProductPriceChangeEvent> pending = eventDao.findPendingProjections(batchSize);
        if (pending == null || pending.isEmpty()) {
            return 0;
        }
        int limit = Math.min(pending.size(), batchSize);
        int projectedCount = 0;
        for (int i = 0; i < limit; i++) {
            ProductPriceChangeEvent event = pending.get(i);
            try {
                projectSingleEvent(event);
                projectedCount++;
            } catch (Exception ex) {
                log.error("Price audit projection failed for eventKey={}, keeping pending for retry",
                        event == null ? null : event.getEventKey(), ex);
            }
        }
        return projectedCount;
    }
}
