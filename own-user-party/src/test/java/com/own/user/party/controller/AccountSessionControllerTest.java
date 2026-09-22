package com.own.user.party.controller;

import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.user.party.auth.service.RefreshSessionService;
import com.own.user.party.service.LoginUserService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AccountSessionControllerTest {
    @Test
    public void buyerCanRevokeEveryRefreshSessionOnlyAfterOwnershipCheck() {
        RefreshSessionService sessions = mock(RefreshSessionService.class); LoginUserService users = mock(LoginUserService.class);
        new AccountSessionController(sessions, users).revokeAll(request("BUYER", "10", "7"));
        verify(users).requireOwnedByActor(eq(7L), any(TradeActor.class));
        verify(sessions).revokeAllForUser(7L);
    }

    @Test
    public void mismatchedUserIdIsRejectedBeforeRevokingSessions() {
        RefreshSessionService sessions = mock(RefreshSessionService.class); LoginUserService users = mock(LoginUserService.class);
        doThrow(TradeException.forbidden("authenticated actor does not own this account"))
                .when(users).requireOwnedByActor(eq(8L), any(TradeActor.class));
        try {
            new AccountSessionController(sessions, users).revokeAll(request("BUYER", "10", "8"));
            fail("mismatched principal must be rejected");
        } catch (TradeException expected) { assertEquals(403, expected.getStatus()); }
    }

    private MockHttpServletRequest request(String actorType, String actorId, String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/v1/account/sessions");
        request.addHeader("X-Actor-Type", actorType); request.addHeader("X-Actor-Id", actorId);
        request.addHeader("X-User-Id", userId); request.addHeader("Idempotency-Key", "session-revoke-all-1");
        return request;
    }
}
