package com.own.user.party.auth.service;

import com.own.face.trade.TradeException;
import com.own.user.party.auth.domain.LoginFailureAttempt;
import com.own.user.party.auth.repository.LoginFailureAttemptRepository;
import java.util.Date;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginAttemptService {
    private final LoginFailureAttemptRepository attempts; private final int maxFailures; private final long lockMinutes;
    public LoginAttemptService(LoginFailureAttemptRepository attempts, @Value("${trade.security.login.max-failures:5}") int maxFailures, @Value("${trade.security.login.lock-minutes:15}") long lockMinutes) {
        this.attempts = attempts; this.maxFailures = Math.max(1, maxFailures); this.lockMinutes = Math.max(1L, lockMinutes);
    }
    public void requireAllowed(String rawLoginName) {
        LoginFailureAttempt attempt = attempts.findByLoginName(normalize(rawLoginName));
        if (attempt != null && attempt.lockedAt(new Date())) throw new TradeException(429, "login is temporarily locked; try again later");
    }
    @Transactional public void failed(String rawLoginName) {
        String loginName = normalize(rawLoginName); if (loginName.isEmpty()) return;
        attempts.insertIfAbsent(loginName); LoginFailureAttempt attempt = attempts.findByLoginNameForUpdate(loginName);
        if (attempt == null) throw new IllegalStateException("login failure record was not created");
        attempt.fail(maxFailures, new Date(System.currentTimeMillis() + lockMinutes * 60L * 1000L)); attempts.save(attempt);
    }
    @Transactional public void succeeded(String rawLoginName) {
        String loginName = normalize(rawLoginName); if (loginName.isEmpty()) return;
        LoginFailureAttempt attempt = attempts.findByLoginNameForUpdate(loginName); if (attempt != null) { attempt.reset(); attempts.save(attempt); }
    }
    private String normalize(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.ROOT); }
}
