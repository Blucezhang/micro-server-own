package com.own.user.party.controller;

import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.face.util.Resp;
import com.own.user.party.auth.service.RefreshSessionService;
import com.own.user.party.service.LoginUserService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AccountSessionControllerTest {
    @Test
    public void buyerCanListOnlyTheirSessionsAfterOwnershipCheck() {
        RefreshSessionService sessions = mock(RefreshSessionService.class); LoginUserService users = mock(LoginUserService.class);
        when(sessions.listActive(7L)).thenReturn(java.util.Collections.singletonList(java.util.Collections.singletonMap("id", 9L)));
        Resp response = new AccountSessionController(sessions, users).list(request("BUYER", "10", "7"));
        verify(users).requireOwnedByActor(eq(7L), any(TradeActor.class));
        verify(sessions).listActive(7L);
        assertNotNull(response.getData());
    }

    @Test
    public void mismatchedActorCannotListSessions() {
        RefreshSessionService sessions = mock(RefreshSessionService.class); LoginUserService users = mock(LoginUserService.class);
        doThrow(TradeException.forbidden("authenticated actor does not own this account"))
                .when(users).requireOwnedByActor(eq(8L), any(TradeActor.class));
        try {
            new AccountSessionController(sessions, users).list(request("BUYER", "10", "8"));
            fail("mismatched actor must be rejected");
        } catch (TradeException expected) { assertEquals(403, expected.getStatus()); }
        verify(sessions, org.mockito.Mockito.never()).listActive(8L);
    }

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

    @Test
    public void revokeOneChecksOwnerBeforeChangingSession() {
        RefreshSessionService sessions = mock(RefreshSessionService.class); LoginUserService users = mock(LoginUserService.class);
        new AccountSessionController(sessions, users).revokeOne(9L, request("BUYER", "10", "7"));
        verify(users).requireOwnedByActor(eq(7L), any(TradeActor.class));
        verify(sessions).revokeOwnedSession(7L, 9L);
    }

    @Test
    public void mismatchedOwnerCannotRevokeOneSession() {
        RefreshSessionService sessions = mock(RefreshSessionService.class); LoginUserService users = mock(LoginUserService.class);
        doThrow(TradeException.forbidden("authenticated actor does not own this account"))
                .when(users).requireOwnedByActor(eq(7L), any(TradeActor.class));
        try {
            new AccountSessionController(sessions, users).revokeOne(9L, request("BUYER", "10", "7"));
            fail("mismatched owner must be rejected");
        } catch (TradeException expected) { assertEquals(403, expected.getStatus()); }
        verify(sessions, org.mockito.Mockito.never()).revokeOwnedSession(7L, 9L);
    }

    private MockHttpServletRequest request(String actorType, String actorId, String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/v1/account/sessions");
        request.addHeader("X-Actor-Type", actorType); request.addHeader("X-Actor-Id", actorId);
        request.addHeader("X-User-Id", userId); request.addHeader("Idempotency-Key", "session-revoke-all-1");
        return request;
    }
}
