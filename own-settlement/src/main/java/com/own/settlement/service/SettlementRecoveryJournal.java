package com.own.settlement.service;

import com.own.settlement.domain.Payment;
import com.own.settlement.domain.RecoveryAction;
import com.own.settlement.domain.RecoveryOperation;
import com.own.settlement.repository.RecoveryOperationRepository;
import java.util.Date;
import com.own.face.trade.config.TradeRecoveryProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementRecoveryJournal {
    private final RecoveryOperationRepository operations;
    private final int maxAttempts;
    private final long retryMillis;
    public SettlementRecoveryJournal(RecoveryOperationRepository operations,
            TradeRecoveryProperties recovery) {
        this.operations = operations; this.maxAttempts = recovery.maxAttempts(); this.retryMillis = recovery.retryDelayMillis();
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int begin(Payment payment, RecoveryAction action) {
        RecoveryOperation operation = operations.findByOperationKey(RecoveryOperation.key(payment.getPaymentNo(), action));
        if (operation == null) operation = new RecoveryOperation(payment, action);
        operation.requireMatches(payment);
        int attempt = operation.attempt(maxAttempts, retryMillis, new Date());
        operations.save(operation);
        return attempt;
    }
    /** Same transaction as payment/refund/receivables: a failed local commit leaves the intent pending. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void complete(Payment payment, RecoveryAction action) {
        RecoveryOperation operation = operations.findByOperationKey(RecoveryOperation.key(payment.getPaymentNo(), action));
        if (operation == null) throw new IllegalStateException("recovery intent is missing");
        operation.requireMatches(payment); operation.complete(); operations.save(operation);
    }
    @Transactional(readOnly = true)
    public boolean hasUnresolvedPayment(String paymentNo) { return operations.hasUnresolvedPayment(paymentNo); }
}
