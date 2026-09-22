package com.own.face.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.own.face.trade.TradeException;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class InternalServiceGuardTest {

    @Test
    public void serviceBoundaryRejectsDirectBusinessRequestsButAllowsTrustedGatewayTraffic() throws Exception {
        String token = "12345678901234567890123456789012";
        ServiceBoundaryFilter filter = new ServiceBoundaryFilter(new InternalServiceGuard(token, ""), true);

        MockHttpServletRequest direct = new MockHttpServletRequest("POST", "/api/v1/orders");
        MockHttpServletResponse denied = new MockHttpServletResponse();
        filter.doFilter(direct, denied, new MockFilterChain());
        assertEquals(403, denied.getStatus());

        MockHttpServletRequest trusted = new MockHttpServletRequest("POST", "/api/v1/orders");
        trusted.addHeader(InternalServiceGuard.HEADER, token);
        MockHttpServletResponse allowed = new MockHttpServletResponse();
        filter.doFilter(trusted, allowed, new MockFilterChain());
        assertEquals(200, allowed.getStatus());
    }
    private static final String CURRENT_TOKEN = "0123456789abcdef0123456789abcdef";
    private static final String PREVIOUS_TOKEN = "fedcba9876543210fedcba9876543210";

    @Test
    public void acceptsExactInternalServiceToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(InternalServiceGuard.HEADER, CURRENT_TOKEN);
        new InternalServiceGuard(CURRENT_TOKEN, "").require(request);
    }

    @Test
    public void rejectsMissingOrIncorrectToken() {
        assertForbidden(new InternalServiceGuard(CURRENT_TOKEN, ""), new MockHttpServletRequest());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(InternalServiceGuard.HEADER, "wrong-secret");
        assertForbidden(new InternalServiceGuard(CURRENT_TOKEN, ""), request);
    }

    @Test
    public void rejectsRequestsWhenServiceTokenIsNotConfigured() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(InternalServiceGuard.HEADER, "anything");
        assertForbidden(new InternalServiceGuard(" ", ""), request);
    }

    @Test
    public void rejectsTooShortConfiguredToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(InternalServiceGuard.HEADER, "short-token");
        assertForbidden(new InternalServiceGuard("short-token", ""), request);
    }

    @Test
    public void acceptsPreviousTokenOnlyDuringRotation() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(InternalServiceGuard.HEADER, PREVIOUS_TOKEN);
        new InternalServiceGuard(CURRENT_TOKEN, PREVIOUS_TOKEN).require(request);
        assertForbidden(new InternalServiceGuard(CURRENT_TOKEN, ""), request);
    }

    private void assertForbidden(InternalServiceGuard guard, MockHttpServletRequest request) {
        try {
            guard.require(request);
            fail("request must be rejected");
        } catch (TradeException expected) {
            assertEquals(403, expected.getStatus());
        }
    }
}
