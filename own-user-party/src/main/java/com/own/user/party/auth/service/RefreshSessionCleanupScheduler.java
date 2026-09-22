package com.own.user.party.auth.service;

import com.own.user.party.auth.repository.RefreshTokenSessionRepository;
import java.util.Date;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Removes expired server-side session records; a refresh JWT remains unusable after expiry. */
@Component
public class RefreshSessionCleanupScheduler {
    private final RefreshTokenSessionRepository sessions;

    public RefreshSessionCleanupScheduler(RefreshTokenSessionRepository sessions) {
        this.sessions = sessions;
    }

    @Scheduled(fixedDelayString = "${trade.security.jwt.refresh-session-cleanup-delay-ms:3600000}")
    public void purgeExpiredSessions() {
        sessions.deleteExpiredBefore(new Date());
    }
}
