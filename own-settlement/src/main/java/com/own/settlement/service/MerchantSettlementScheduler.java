package com.own.settlement.service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
/** Daily scan settles receivables that have completed the configured cycle. */
@Component
public class MerchantSettlementScheduler {
    private final MerchantSettlementService service;
    public MerchantSettlementScheduler(MerchantSettlementService service) { this.service = service; }
    @Scheduled(fixedDelayString = "${trade.settlement.scan-fixed-delay-millis:86400000}")
    public void settleDue() { service.settleDue(null); }
}
