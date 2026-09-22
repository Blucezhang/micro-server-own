package com.own.user.party.controller;

import com.own.face.trade.TradeException;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.dto.OwnProfileCommand;
import com.own.user.party.service.LoginUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

public class AccountProfileControllerTest {
    private LoginUserService users;
    private AccountProfileController controller;

    @BeforeEach
    public void setUp() { users = mock(LoginUserService.class); controller = new AccountProfileController(users); }

    @Test
    public void buyerCanUpdateOnlyTheirJwtUserProfile() {
        LoginUser user = new LoginUser(); user.setLoginUserId(7L); user.setName("Alice");
        when(users.updateOwnProfile(eq(7L), any(OwnProfileCommand.class))).thenReturn(user);
        OwnProfileCommand command = new OwnProfileCommand(); command.setName("Alice");

        controller.update(command, request("BUYER", "11", "7"));

        verify(users).updateOwnProfile(eq(7L), any(OwnProfileCommand.class));
    }

    @Test
    public void systemCannotUseMarketplaceSelfServiceProfile() {
        doThrow(TradeException.forbidden("buyer or merchant actor is required"))
                .when(users).requireOwnedByActor(eq(1L), any(com.own.face.trade.TradeActor.class));
        try {
            controller.get(request("SYSTEM", "1", "1"));
            fail("system actor must be denied");
        } catch (TradeException expected) {
            assertEquals(403, expected.getStatus());
        }
    }

    private MockHttpServletRequest request(String actorType, String actorId, String userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Actor-Type", actorType); request.addHeader("X-Actor-Id", actorId);
        request.addHeader("X-User-Id", userId); request.addHeader("Idempotency-Key", "profile-update-1");
        return request;
    }
}
