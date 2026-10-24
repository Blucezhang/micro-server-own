package com.own.promotion.coupon.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.own.face.security.InternalServiceGuard;
import com.own.face.trade.TradeException;
import com.own.promotion.coupon.dto.CouponConsumptionCommand;
import com.own.promotion.coupon.service.CouponService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CouponConsumptionControllerTest {
    private static final String TOKEN = "local-test-internal-token-32-bytes-minimum";
    private final CouponService service = mock(CouponService.class);
    private final CouponController controller = new CouponController(service, new InternalServiceGuard(TOKEN, ""));

    @Test
    void requiresInternalTokenAndSystemActor() {
        CouponConsumptionCommand command = command();
        assertEquals(403, assertThrows(TradeException.class,
                () -> controller.consume(command, request(null, "SYSTEM"))).getStatus());
        assertEquals(403, assertThrows(TradeException.class,
                () -> controller.consume(command, request(TOKEN, "BUYER"))).getStatus());
    }

    @Test
    void sendsTheConfirmedDiscountToCouponService() {
        controller.consume(command(), request(TOKEN, "SYSTEM"));
        verify(service).consumeOrder("ORD-1", BigDecimal.ONE);
    }

    private CouponConsumptionCommand command() {
        CouponConsumptionCommand command = new CouponConsumptionCommand();
        command.setOrderNo("ORD-1");
        command.setExpectedDiscount(BigDecimal.ONE);
        return command;
    }

    private HttpServletRequest request(String token, String actorType) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Internal-Service-Token")).thenReturn(token);
        when(request.getHeader("X-Actor-Id")).thenReturn("1");
        when(request.getHeader("X-Actor-Type")).thenReturn(actorType);
        when(request.getHeader("Idempotency-Key")).thenReturn("order-coupon-consume-ORD-1");
        return request;
    }
}
