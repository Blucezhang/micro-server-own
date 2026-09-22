package com.own.order.service;

import com.own.order.domain.AfterSale;
import com.own.order.domain.AfterSaleStatus;
import com.own.order.repository.AfterSaleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Replays a local simulated refund request after a transient settlement outage.
 * The settlement and inventory ledgers make the downstream callback idempotent.
 */
@Component
public class AfterSaleRefundRetryScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(AfterSaleRefundRetryScheduler.class);
    private final AfterSaleRepository sales;
    private final SettlementClient settlement;

    public AfterSaleRefundRetryScheduler(AfterSaleRepository sales, SettlementClient settlement) {
        this.sales = sales;
        this.settlement = settlement;
    }

    @Scheduled(fixedDelayString = "${trade.after-sale.refund-retry-delay-millis:60000}")
    public void retryPendingRefunds() {
        for (AfterSale sale : sales.findByStatus(AfterSaleStatus.REFUND_PENDING)) {
            try {
                settlement.refundAfterSale(sale);
            } catch (RuntimeException exception) {
                LOG.warn("Simulated after-sale refund dispatch will be retried: {} ({})",
                        sale.getAfterSaleNo(), exception.getMessage());
            }
        }
    }
}
