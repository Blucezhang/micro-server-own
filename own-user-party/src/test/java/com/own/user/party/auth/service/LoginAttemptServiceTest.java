package com.own.user.party.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.own.face.trade.TradeException;
import com.own.user.party.auth.domain.LoginFailureAttempt;
import com.own.user.party.auth.repository.LoginFailureAttemptRepository;
import org.junit.jupiter.api.Test;

public class LoginAttemptServiceTest {
    @Test
    public void reachesConfiguredThresholdThenRejectsLogin() {
        LoginFailureAttemptRepository attempts = mock(LoginFailureAttemptRepository.class);
        LoginFailureAttempt attempt = new LoginFailureAttempt("buyer");
        when(attempts.findByLoginNameForUpdate("buyer")).thenReturn(attempt);
        when(attempts.findByLoginName("buyer")).thenReturn(attempt);
        LoginAttemptService service = new LoginAttemptService(attempts, 2, 15);
        service.failed("BUYER"); service.failed("buyer");
        assertEquals(Integer.valueOf(2), attempt.getFailureCount());
        try { service.requireAllowed("buyer"); fail("locked account must reject login"); }
        catch (TradeException expected) { assertEquals(429, expected.getStatus()); }
    }
}
