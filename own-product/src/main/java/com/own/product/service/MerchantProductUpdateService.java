package com.own.product.service;

import com.own.product.domain.Product;
import com.own.product.domain.ProductPriceChangeEvent;
import com.own.product.dto.MerchantProductCommand;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MerchantProductUpdateService {

    private final ProductUpdateGraphTransactionService graphTxService;
    private final ProductPriceAuditProjector projector;

    @Autowired
    public MerchantProductUpdateService(
            ProductUpdateGraphTransactionService graphTxService,
            ProductPriceAuditProjector projector) {
        this.graphTxService = Objects.requireNonNull(graphTxService, "graphTxService is required");
        this.projector = Objects.requireNonNull(projector, "projector is required");
    }

    public Product updateProduct(Long productId, Long merchantId, String idempotencyKey, MerchantProductCommand command) {
        ProductUpdateGraphTransactionService.Outcome outcome =
                graphTxService.executeUpdate(productId, merchantId, idempotencyKey, command);
        ProductPriceChangeEvent event = outcome.getEvent();
        if (Boolean.TRUE.equals(event.getPriceChanged()) && !Boolean.TRUE.equals(event.getProjected())) {
            try {
                projector.projectSingleEvent(event);
            } catch (Exception ex) {
                log.error("Synchronous price audit projection failed for eventKey={}, pending recovery",
                        event.getEventKey(), ex);
            }
        }
        return outcome.getProduct();
    }

    public Product changeSaleStatus(Long productId, Long merchantId, String value) {
        return graphTxService.changeSaleStatus(productId, merchantId, value);
    }
}
