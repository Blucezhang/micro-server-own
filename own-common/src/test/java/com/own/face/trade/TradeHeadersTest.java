package com.own.face.trade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class TradeHeadersTest {

    @Test
    public void parsesRequiredActorHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Actor-Id", "12");
        request.addHeader("X-Actor-Type", "buyer");
        request.addHeader("Idempotency-Key", "create-order-1");

        TradeActor actor = TradeHeaders.actor(request);
        assertEquals(Long.valueOf(12L), actor.getId());
        assertEquals(ActorType.BUYER, actor.getType());
        assertEquals("create-order-1", TradeHeaders.idempotencyKey(request));
    }

    @Test
    public void rejectsMissingIdempotencyKey() {
        try {
            TradeHeaders.idempotencyKey(new MockHttpServletRequest());
            fail("expected validation error");
        } catch (TradeException expected) {
            assertEquals(400, expected.getStatus());
        }
    }
}
