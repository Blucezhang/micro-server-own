package com.own.order.controller;

import com.own.face.security.InternalServiceGuard;
import com.own.face.trade.TradeException;
import com.own.order.domain.TradeOrder;
import com.own.order.repository.TradeOrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderCancellationRecoveryControllerTest {
    private static final String TOKEN = "test-internal-token-at-least-32-bytes";
    @Test
    void requiresInternalTokenAndSystemActor() {
        var orders = mock(TradeOrderRepository.class);
        var controller = new OrderCancellationRecoveryController(orders, new InternalServiceGuard(TOKEN, ""));
        var request = request("SYSTEM");
        request.removeHeader(InternalServiceGuard.HEADER);
        assertThrows(TradeException.class, () -> controller.list("PENDING", request));
        assertThrows(TradeException.class, () -> controller.list("PENDING", request("BUYER")));
        verifyNoInteractions(orders);
    }
    @Test
    void rejectsUnboundedStatusAndReturnsOnlyRecoveryMetadata() {
        var orders = mock(TradeOrderRepository.class);
        var controller = new OrderCancellationRecoveryController(orders, new InternalServiceGuard(TOKEN, ""));
        assertThrows(TradeException.class, () -> controller.list("ALL", request("SYSTEM")));
        TradeOrder order = new TradeOrder("ORD-1", 7L, BigDecimal.TEN, BigDecimal.ZERO, "private address", "key");
        order.beginCancellation(1, false); order.beginCancellation(1, false);
        when(orders.findTop100ByCancellationRecoveryStatusOrderByIdDesc("RECONCILIATION_REQUIRED")).thenReturn(List.of(order));
        var result = (List<?>) controller.list("RECONCILIATION_REQUIRED", request("SYSTEM")).getData();
        var view = (OrderCancellationRecoveryController.CancellationView) result.get(0);
        assertEquals("ORD-1", view.orderNo()); assertEquals(1, view.attempts());
        assertFalse(view.toString().contains("private address"));
        assertEquals(5, view.getClass().getRecordComponents().length);
    }
    private MockHttpServletRequest request(String actor) {
        var request = new MockHttpServletRequest();
        request.addHeader(InternalServiceGuard.HEADER, TOKEN);
        request.addHeader("X-Actor-Type", actor); request.addHeader("X-Actor-Id", "7");
        return request;
    }
}
