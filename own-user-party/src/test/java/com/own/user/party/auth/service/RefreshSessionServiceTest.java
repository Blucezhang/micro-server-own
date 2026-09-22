package com.own.user.party.auth.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.face.security.JwtRefreshPrincipal;
import com.own.face.security.JwtTokenService;
import com.own.face.trade.ActorType;
import com.own.face.trade.TradeException;
import com.own.user.party.auth.domain.RefreshTokenSession;
import com.own.user.party.auth.repository.RefreshTokenSessionRepository;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.service.LoginAuthorization;
import com.own.user.party.service.LoginUserService;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class RefreshSessionServiceTest {
    @Test
    public void activeSessionListDoesNotExposeTokenIdentifier() {
        JwtTokenService tokens = new JwtTokenService(new ObjectMapper(), "01234567890123456789012345678901", 120L, "issuer");
        RefreshTokenSessionRepository sessions = mock(RefreshTokenSessionRepository.class);
        RefreshTokenSession session = new RefreshTokenSession("private-jti", 7L, "BUYER", tokens.refreshExpiresAt());
        ReflectionTestUtils.setField(session, "id", 9L);
        when(sessions.findByUserIdAndStatusOrderByCreatedAtDesc(7L, "ACTIVE")).thenReturn(Collections.singletonList(session));
        java.util.List<java.util.Map<String, Object>> listed = new RefreshSessionService(tokens, sessions, mock(LoginUserService.class)).listActive(7L);
        assertEquals(1, listed.size()); assertEquals(9L, listed.get(0).get("id")); assertTrue(!listed.get(0).containsKey("tokenId"));
    }

    @Test
    public void rotationReplacesOldSessionAndRejectsItsReuse() {
        JwtTokenService tokens = new JwtTokenService(new ObjectMapper(), "01234567890123456789012345678901", 120L, "issuer");
        RefreshTokenSessionRepository sessions = mock(RefreshTokenSessionRepository.class);
        LoginUserService users = mock(LoginUserService.class);
        RefreshSessionService service = new RefreshSessionService(tokens, sessions, users);
        LoginUser user = new LoginUser(); user.setLoginUserId(7L); user.setPartyId(70L);
        LoginAuthorization authorization = new LoginAuthorization(70L, ActorType.BUYER, Arrays.asList("ROLE_BUYER"), Arrays.asList("order:purchase"));
        String original = (String) service.issue(user, authorization).get("refreshToken");
        JwtRefreshPrincipal principal = tokens.verifyRefresh(original);
        RefreshTokenSession existing = new RefreshTokenSession(principal.getTokenId(), 7L, "BUYER", principal.getExpiresAt());
        when(sessions.findByTokenIdForUpdate(principal.getTokenId())).thenReturn(existing);
        when(sessions.save(any(RefreshTokenSession.class))).thenAnswer(i -> i.getArguments()[0]);
        when(users.getById(7L)).thenReturn(user);
        when(users.authorization(user, "BUYER")).thenReturn(authorization);

        java.util.Map<String, Object> rotated = service.rotate(original);
        assertEquals("REPLACED", existing.getStatus());
        assertTrue(((String) rotated.get("refreshToken")).length() > 20);
        try { service.rotate(original); } catch (TradeException expected) { assertEquals(403, expected.getStatus()); return; }
        throw new AssertionError("reused refresh token must be rejected");
    }
}
