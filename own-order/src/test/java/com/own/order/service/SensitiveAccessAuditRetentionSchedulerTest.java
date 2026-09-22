package com.own.order.service;

import com.own.order.repository.SensitiveAccessAuditRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SensitiveAccessAuditRetentionSchedulerTest {
    @Test
    public void positiveRetentionDeletesOnlyRecordsBeforeItsCutoff() {
        SensitiveAccessAuditRepository repository = Mockito.mock(SensitiveAccessAuditRepository.class);
        new SensitiveAccessAuditRetentionScheduler(repository, 180).purgeExpired();
        Mockito.verify(repository).deleteByCreatedAtBefore(Mockito.any(java.util.Date.class));
    }

    @Test
    public void nonPositiveRetentionDisablesDeletion() {
        SensitiveAccessAuditRepository repository = Mockito.mock(SensitiveAccessAuditRepository.class);
        new SensitiveAccessAuditRetentionScheduler(repository, 0).purgeExpired();
        Mockito.verify(repository, Mockito.never()).deleteByCreatedAtBefore(Mockito.any(java.util.Date.class));
    }
}
