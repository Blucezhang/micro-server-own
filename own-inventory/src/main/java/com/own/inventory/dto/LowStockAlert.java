package com.own.inventory.dto;

import com.own.inventory.domain.InventoryStock;
import com.own.inventory.domain.LowStockAlertRule;

/** Current-state read model; it deliberately does not imply that an external notification was sent. */
public class LowStockAlert {
    private final Long productId;
    private final Long merchantId;
    private final Integer thresholdQuantity;
    private final Integer availableQuantity;
    private final Integer reservedQuantity;
    private final Integer soldQuantity;

    public LowStockAlert(LowStockAlertRule rule, InventoryStock stock) {
        this.productId = rule.getProductId();
        this.merchantId = rule.getMerchantId();
        this.thresholdQuantity = rule.getThresholdQuantity();
        this.availableQuantity = stock.getAvailableQuantity();
        this.reservedQuantity = stock.getReservedQuantity();
        this.soldQuantity = stock.getSoldQuantity();
    }

    public Long getProductId() { return productId; }
    public Long getMerchantId() { return merchantId; }
    public Integer getThresholdQuantity() { return thresholdQuantity; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public Integer getSoldQuantity() { return soldQuantity; }
}
