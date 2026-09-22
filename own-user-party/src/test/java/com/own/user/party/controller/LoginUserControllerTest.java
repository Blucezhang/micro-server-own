package com.own.user.party.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.face.party.UserBean;
import com.own.face.util.Resp;
import com.own.face.trade.TradeException;
import com.own.face.party.LoginUserBean;
import com.own.user.party.dao.LoginUserDao;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.service.LoginUserService;
import com.own.user.party.service.LoginAuthorization;
import com.own.face.security.JwtTokenService;
import com.own.face.trade.ActorType;
import com.own.user.party.dto.TokenLoginCommand;
import com.own.user.party.auth.service.RefreshSessionService;
import com.own.user.party.auth.service.LoginAttemptService;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

public class LoginUserControllerTest {

    private LoginUserService loginUserService;
    private LoginUserController controller;
    private ObjectMapper objectMapper;
    private RefreshSessionService refreshSessions;
    private LoginAttemptService loginAttempts;

    @BeforeEach
    public void setUp() {
        loginUserService = mock(LoginUserService.class);
        refreshSessions = mock(RefreshSessionService.class);
        loginAttempts = mock(LoginAttemptService.class);
        objectMapper = new ObjectMapper();
        controller = new LoginUserController(loginUserService, mock(LoginUserDao.class),
                new JwtTokenService(objectMapper, "01234567890123456789012345678901", 7200L, "test"), refreshSessions, loginAttempts);
    }

    @Test
    public void loginResponseDoesNotExposeStoredPasswords() throws Exception {
        UserBean request = credentials("alice", "submitted-secret", null);
        LoginUser storedUser = new LoginUser();
        storedUser.setLoginName("alice");
        storedUser.setPassword("stored-secret");
        storedUser.setTrspwd("transaction-secret");
        when(loginUserService.authenticate("alice", "submitted-secret")).thenReturn(storedUser);

        String json = objectMapper.writeValueAsString(controller.login(request));

        assertFalse(json.contains("submitted-secret"));
        assertFalse(json.contains("stored-secret"));
        assertFalse(json.contains("transaction-secret"));
        assertFalse(json.contains("newpassword"));
    }

    @Test
    public void passwordChangeResponseDoesNotEchoEitherPassword() throws Exception {
        UserBean request = credentials("alice", "old-secret", "new-secret");
        LoginUser changed = new LoginUser(); changed.setLoginUserId(7L);
        when(loginUserService.changePasswordAndGet("alice", "old-secret", "new-secret")).thenReturn(changed);

        String json = objectMapper.writeValueAsString(controller.updatePassword(request));

        assertFalse(json.contains("old-secret"));
        assertFalse(json.contains("new-secret"));
        assertFalse(json.contains("newpassword"));
    }

    @Test
    public void failedPasswordChangeParticipatesInLoginFailureProtection() {
        UserBean request = credentials("alice", "wrong-secret", "new-secret");
        when(loginUserService.changePasswordAndGet("alice", "wrong-secret", "new-secret")).thenReturn(null);

        controller.updatePassword(request);

        verify(loginAttempts).requireAllowed("alice");
        verify(loginAttempts).failed("alice");
    }

    @Test
    public void successfulPasswordChangeClearsLoginFailureProtection() {
        UserBean request = credentials("alice", "old-secret", "new-secret");
        LoginUser changed = new LoginUser(); changed.setLoginUserId(7L);
        when(loginUserService.changePasswordAndGet("alice", "old-secret", "new-secret")).thenReturn(changed);

        controller.updatePassword(request);

        verify(loginAttempts).requireAllowed("alice");
        verify(loginAttempts).succeeded("alice");
    }

    @Test
    public void existingSessionEndpointAlsoChecksJwtActorOwnership() {
        controller.activeSessions(7L, request("BUYER", "70"));
        verify(loginUserService).requireOwnedByActor(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any(com.own.face.trade.TradeActor.class));
        verify(refreshSessions).listActive(7L);
    }

    @Test
    public void existingSessionEndpointRejectsMismatchedJwtActor() {
        doThrow(TradeException.forbidden("authenticated actor does not own this account"))
                .when(loginUserService).requireOwnedByActor(org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any(com.own.face.trade.TradeActor.class));
        try {
            controller.revokeSession(7L, 9L, request("BUYER", "71"));
            fail("mismatched actor must be rejected");
        } catch (TradeException expected) { assertEquals(403, expected.getStatus()); }
        verify(refreshSessions, never()).revokeOwnedSession(7L, 9L);
    }

    @Test
    public void failedLoginUsesGenericAuthenticationMessage() {
        UserBean request = credentials("missing", "wrong-secret", null);
        when(loginUserService.authenticate("missing", "wrong-secret")).thenReturn(null);

        Resp response = controller.login(request);

        assertEquals("authentication_failed", ((java.util.Map) response.getData()).get("message"));
    }

    @Test
    public void updatingMissingUserReturnsExplicitNotFound() {
        LoginUserBean request = new LoginUserBean();
        when(loginUserService.updateProfile(99L, request)).thenReturn(null);
        try {
            controller.updateLoginUser(request, 99L);
            fail("missing user must not be silently accepted");
        } catch (TradeException expected) {
            assertEquals(404, expected.getStatus());
        }
    }

    @Test
    public void tokenLoginReturnsBearerTokenWithoutPassword() throws Exception {
        LoginUser user = new LoginUser(); user.setLoginUserId(7L); user.setPartyId(70L); user.setPassword("stored-hash");
        TokenLoginCommand command = new TokenLoginCommand(); command.setLoginUserName("buyer"); command.setPassword("plain-password"); command.setActorType("BUYER");
        when(loginUserService.authenticate("buyer", "plain-password")).thenReturn(user);
        when(loginUserService.authorization(user, "BUYER")).thenReturn(new LoginAuthorization(70L, ActorType.BUYER,
                Arrays.asList("ROLE_BUYER"), Arrays.asList("order:read")));
        java.util.Map<String,Object> tokens = new LinkedHashMap<String,Object>(); tokens.put("accessToken", "token"); tokens.put("refreshToken", "refresh");
        when(refreshSessions.issue(org.mockito.ArgumentMatchers.eq(user), org.mockito.ArgumentMatchers.any(LoginAuthorization.class))).thenReturn(tokens);
        String json = objectMapper.writeValueAsString(controller.token(command));
        assertFalse(json.contains("plain-password"));
        assertFalse(json.contains("stored-hash"));
        assertTrue(json.contains("accessToken"));
    }

    private UserBean credentials(String loginName, String password, String newPassword) {
        UserBean request = new UserBean();
        request.setLoginUserName(loginName);
        request.setPassword(password);
        request.setNewpassword(newPassword);
        return request;
    }

    private MockHttpServletRequest request(String actorType, String actorId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Actor-Type", actorType); request.addHeader("X-Actor-Id", actorId);
        request.addHeader("Idempotency-Key", "session-revoke-1");
        return request;
    }
}
