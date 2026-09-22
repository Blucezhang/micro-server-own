package com.own.inventory.service;

import com.own.face.trade.TradeReliabilityMetrics;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InventoryExpiryScheduler {
    private final InventoryService inventoryService;
    private final TradeReliabilityMetrics metrics;
    public InventoryExpiryScheduler(InventoryService inventoryService, TradeReliabilityMetrics metrics) {
        this.inventoryService = inventoryService; this.metrics = metrics;
    }
    @Scheduled(fixedDelayString = "${trade.inventory.expiry-delay-millis:60000}")
    public void releaseExpiredReservations() { metrics.expiredReservations(inventoryService.expireReservations()); }
}
