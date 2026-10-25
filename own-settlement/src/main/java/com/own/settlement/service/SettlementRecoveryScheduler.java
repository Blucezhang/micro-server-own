package com.own.settlement.service;

import com.own.face.trade.recovery.RecoveryStatus;
import com.own.settlement.domain.RecoveryOperation;
import com.own.settlement.repository.RecoveryOperationRepository;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SettlementRecoveryScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(SettlementRecoveryScheduler.class);
    private final RecoveryOperationRepository operations;
    private final SettlementService settlement;
    public SettlementRecoveryScheduler(RecoveryOperationRepository operations, SettlementService settlement) {
        this.operations = operations; this.settlement = settlement;
    }
    @Scheduled(fixedDelayString = "${trade.recovery.scan-delay-millis}", scheduler = "tradeRecoveryScheduler")
    public void recover() {
        for (RecoveryOperation operation : operations.findTop50ByStatusAndNextAttemptAtBeforeOrderByIdAsc(RecoveryStatus.PENDING, new Date())) {
            try { settlement.recover(operation.getPaymentNo(), operation.getAction()); }
            catch (RuntimeException exception) { LOG.warn("Settlement recovery failed for {}", operation.getOperationKey(), exception); }
        }
    }
}
