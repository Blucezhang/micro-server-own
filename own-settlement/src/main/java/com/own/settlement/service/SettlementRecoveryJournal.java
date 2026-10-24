package com.own.settlement.service;

import com.own.settlement.domain.Payment;
import com.own.settlement.domain.RecoveryAction;
import com.own.settlement.domain.RecoveryOperation;
import com.own.settlement.repository.RecoveryOperationRepository;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettlementRecoveryJournal {
    private final RecoveryOperationRepository operations;
    private final int maxAttempts;
    private final long retryMillis;
    public SettlementRecoveryJournal(RecoveryOperationRepository operations,
            @Value("${trade.recovery.max-attempts:8}") int maxAttempts,
            @Value("${trade.recovery.retry-delay-millis:30000}") long retryMillis) {
        if (maxAttempts < 1 || retryMillis < 1000 || retryMillis > 86_400_000) throw new IllegalArgumentException("invalid recovery limits");
        this.operations = operations; this.maxAttempts = maxAttempts; this.retryMillis = retryMillis;
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
