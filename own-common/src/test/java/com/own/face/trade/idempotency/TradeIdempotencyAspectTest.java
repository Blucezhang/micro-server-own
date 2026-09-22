package com.own.face.trade.idempotency;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class TradeIdempotencyAspectTest {

    @Test
    public void onlyStateChangingTradeCommandsEnterIdempotencyStore() {
        assertTrue(write("/order/api/v1/orders"));
        assertTrue(write("/inventory/api/v1/stocks/1/adjustments"));
        assertFalse(write("/order/api/v1/checkouts/quote"));
        assertFalse(write("/sale/api/v1/coupons/internal/quotes"));
        assertFalse(write("/settlement/api/v1/payments/mock-callbacks/MOCK_WECHAT"));
    }

    @Test
    public void buyerRegistrationIsTheOnlyAnonymousIdempotentCommand() {
        assertTrue(TradeIdempotencyAspect.isAnonymousBuyerRegistration(new MockHttpServletRequest("POST", "/user/api/v1/auth/registrations")));
        assertTrue(TradeIdempotencyAspect.isAnonymousBuyerRegistration(new MockHttpServletRequest("POST", "/api/v1/auth/registrations")));
        assertFalse(TradeIdempotencyAspect.isAnonymousBuyerRegistration(new MockHttpServletRequest("POST", "/user/api/v1/auth/registration")));
        assertFalse(TradeIdempotencyAspect.isAnonymousBuyerRegistration(new MockHttpServletRequest("GET", "/user/api/v1/auth/registrations")));
    }

    private boolean write(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        return TradeIdempotencyAspect.isTradeWrite(request);
    }
}
