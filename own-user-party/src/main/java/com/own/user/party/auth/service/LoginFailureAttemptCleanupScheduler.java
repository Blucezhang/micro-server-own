package com.own.user.party.auth.service;

import com.own.user.party.auth.repository.LoginFailureAttemptRepository;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Bounds retained account-throttle metadata without weakening an active lock. */
@Component
public class LoginFailureAttemptCleanupScheduler {
    private final LoginFailureAttemptRepository attempts;
    private final long retentionDays;

    public LoginFailureAttemptCleanupScheduler(LoginFailureAttemptRepository attempts,
                                               @Value("${trade.security.login.attempt-retention-days:90}") long retentionDays) {
        this.attempts = attempts;
        this.retentionDays = Math.min(3650L, Math.max(1L, retentionDays));
    }

    @Scheduled(fixedDelayString = "${trade.security.login.attempt-cleanup-delay-ms:86400000}")
    public void purgeExpiredAttempts() {
        Date now = new Date();
        attempts.deleteInactiveBefore(new Date(now.getTime() - retentionDays * 24L * 60L * 60L * 1000L), now);
    }
}
