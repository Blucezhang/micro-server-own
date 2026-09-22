package com.own.user.party.auth.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.own.user.party.auth.repository.LoginFailureAttemptRepository;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class LoginFailureAttemptCleanupSchedulerTest {
    @Test
    public void removesOnlyRecordsOlderThanConfiguredRetention() {
        LoginFailureAttemptRepository attempts = mock(LoginFailureAttemptRepository.class);
        new LoginFailureAttemptCleanupScheduler(attempts, 30).purgeExpiredAttempts();

        ArgumentCaptor<Date> cutoff = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> now = ArgumentCaptor.forClass(Date.class);
        verify(attempts).deleteInactiveBefore(cutoff.capture(), now.capture());
        long age = now.getValue().getTime() - cutoff.getValue().getTime();
        assertTrue(age >= 30L * 24L * 60L * 60L * 1000L - 1000L,
                "retention cutoff must be approximately 30 days");
    }
}
