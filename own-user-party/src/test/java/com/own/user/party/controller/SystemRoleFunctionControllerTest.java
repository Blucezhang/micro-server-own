package com.own.user.party.controller;

import com.own.face.trade.TradeException;
import com.own.user.party.dto.RoleFunctionGrantCommand;
import com.own.user.party.service.RoleFunctionGrantService;
import java.util.Arrays;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SystemRoleFunctionControllerTest {
    @Test
    public void systemCanAppendRoleFunctions() {
        RoleFunctionGrantService grants = mock(RoleFunctionGrantService.class);
        when(grants.grant(7L, Arrays.asList(3L))).thenReturn(Arrays.asList(3L));
        SystemRoleFunctionController controller = new SystemRoleFunctionController(grants);
        controller.grant(7L, command(), request("SYSTEM"));
        verify(grants).grant(7L, Arrays.asList(3L));
    }

    @Test
    public void buyerCannotAppendRoleFunctions() {
        try {
            new SystemRoleFunctionController(mock(RoleFunctionGrantService.class)).grant(7L, command(), request("BUYER"));
            fail("buyer must not modify roles");
        } catch (TradeException expected) { assertEquals(403, expected.getStatus()); }
    }

    private RoleFunctionGrantCommand command() {
        RoleFunctionGrantCommand command = new RoleFunctionGrantCommand(); command.setFunctionIds(Arrays.asList(3L)); return command;
    }

    private MockHttpServletRequest request(String actorType) {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/v1/system/roles/7/functions");
        request.addHeader("X-Actor-Id", "1"); request.addHeader("X-Actor-Type", actorType);
        request.addHeader("Idempotency-Key", "role-function-grant-1"); return request;
    }
}
