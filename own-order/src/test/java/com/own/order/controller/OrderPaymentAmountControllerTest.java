package com.own.order.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.own.face.security.InternalServiceGuard;
import com.own.face.trade.TradeException;
import com.own.face.util.Resp;
import com.own.order.service.OrderOutboxService;
import com.own.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderPaymentAmountControllerTest {
    private static final String TOKEN = "local-test-internal-token-32-bytes-minimum";
    private final OrderService orders = mock(OrderService.class);
    private final OrderController controller = new OrderController(orders, mock(OrderOutboxService.class),
            new InternalServiceGuard(TOKEN, ""));

    @Test
    void requiresInternalTokenAndSystemActor() {
        assertEquals(403, assertThrows(TradeException.class,
                () -> controller.payableAmount("ORD-1", 7L, request(null, "SYSTEM"))).getStatus());
        assertEquals(403, assertThrows(TradeException.class,
                () -> controller.payableAmount("ORD-1", 7L, request(TOKEN, "BUYER"))).getStatus());
    }

    @Test
    void passesBuyerScopeOnlyAfterInternalAuthentication() {
        when(orders.paymentAmount(7L, "ORD-1")).thenReturn(new BigDecimal("17.90"));

        Resp result = controller.payableAmount("ORD-1", 7L, request(TOKEN, "SYSTEM"));

        assertEquals(new BigDecimal("17.90"), result.getData());
        verify(orders).paymentAmount(7L, "ORD-1");
    }

    private HttpServletRequest request(String token, String actorType) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Internal-Service-Token")).thenReturn(token);
        when(request.getHeader("X-Actor-Id")).thenReturn("1");
        when(request.getHeader("X-Actor-Type")).thenReturn(actorType);
        return request;
    }

    @Test
    void recoverySnapshotRequiresInternalSystemActorAndUsesTheMinimalProjection() {
        assertEquals(403, assertThrows(TradeException.class,
                () -> controller.recoverySnapshot("ORD-1", request(null, "SYSTEM"))).getStatus());
        assertEquals(403, assertThrows(TradeException.class,
                () -> controller.recoverySnapshot("ORD-1", request(TOKEN, "BUYER"))).getStatus());
        var snapshot = new com.own.face.trade.OrderRecoverySnapshot("ORD-1", 7L, BigDecimal.TEN, "PAID", "COMPLETED");
        when(orders.recoverySnapshot(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.eq("ORD-1"))).thenReturn(snapshot);
        assertEquals(snapshot, controller.recoverySnapshot("ORD-1", request(TOKEN, "SYSTEM")).getData());
        assertEquals(java.util.Set.of("orderNo", "buyerId", "payableAmount", "status", "paymentRecoveryStatus"),
                java.util.Arrays.stream(snapshot.getClass().getRecordComponents()).map(java.lang.reflect.RecordComponent::getName)
                        .collect(java.util.stream.Collectors.toSet()));
    }
}
