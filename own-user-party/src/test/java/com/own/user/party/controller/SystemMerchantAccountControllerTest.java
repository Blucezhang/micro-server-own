package com.own.user.party.controller;

import com.own.face.trade.TradeException;
import com.own.user.party.service.MerchantRoleGrantService;
import java.util.Collections;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SystemMerchantAccountControllerTest {
    @Test
    public void systemCanGrantMerchantRole() {
        MerchantRoleGrantService service = mock(MerchantRoleGrantService.class);
        when(service.grantMerchantRole(7L)).thenReturn(Collections.<String, Object>emptyMap());
        new SystemMerchantAccountController(service).grantMerchantRole(7L, request("SYSTEM"));
        verify(service).grantMerchantRole(7L);
    }

    @Test
    public void buyerCannotGrantMerchantRole() {
        try {
            new SystemMerchantAccountController(mock(MerchantRoleGrantService.class)).grantMerchantRole(7L, request("BUYER"));
            fail("buyer must not administer merchant roles");
        } catch (TradeException expected) { assertEquals(403, expected.getStatus()); }
    }

    private MockHttpServletRequest request(String actorType) {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/v1/system/accounts/7/merchant-role");
        request.addHeader("X-Actor-Id", "1"); request.addHeader("X-Actor-Type", actorType);
        request.addHeader("Idempotency-Key", "merchant-role-grant-1");
        return request;
    }
}
