package com.own.user.party.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.own.face.trade.ActorType;
import com.own.face.util.Resp;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.service.LoginAuthorization;
import com.own.user.party.service.LoginUserService;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class AccountAuthorizationControllerTest {
    private LoginUserService users;
    private AccountAuthorizationController controller;

    @BeforeEach public void setUp() { users = mock(LoginUserService.class); controller = new AccountAuthorizationController(users); }

    @Test
    public void returnsOnlyTheAuthenticatedAccountsPersistedAuthorization() {
        LoginUser user = new LoginUser(); user.setLoginUserId(7L); user.setPartyId(11L);
        when(users.requireAuthorizationActor(eq(7L), any(com.own.face.trade.TradeActor.class))).thenReturn(user);
        when(users.authorization(user, "BUYER")).thenReturn(new LoginAuthorization(11L, ActorType.BUYER,
                Arrays.asList("ROLE_BUYER"), Collections.<String>emptyList()));

        Resp response = controller.current(request("BUYER", "11", "7"));
        Map data = (Map) response.getData();
        assertEquals(Long.valueOf(7L), data.get("userId"));
        assertEquals("BUYER", String.valueOf(data.get("actorType")));
        verify(users).requireAuthorizationActor(eq(7L), any(com.own.face.trade.TradeActor.class));
    }

    @Test
    public void systemCanReadItsOwnAuthorizationProjection() {
        LoginUser user = new LoginUser(); user.setLoginUserId(1L);
        when(users.requireAuthorizationActor(eq(1L), any(com.own.face.trade.TradeActor.class))).thenReturn(user);
        when(users.authorization(user, "SYSTEM")).thenReturn(new LoginAuthorization(1L, ActorType.SYSTEM,
                Arrays.asList("ROLE_SYSTEM"), Collections.<String>emptyList()));
        Resp response = controller.current(request("SYSTEM", "1", "1"));
        assertEquals("SYSTEM", String.valueOf(((Map) response.getData()).get("actorType")));
        verify(users).requireAuthorizationActor(eq(1L), any(com.own.face.trade.TradeActor.class));
    }

    private MockHttpServletRequest request(String actorType, String actorId, String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Actor-Type", actorType); request.addHeader("X-Actor-Id", actorId); request.addHeader("X-User-Id", userId);
        return request;
    }
}
