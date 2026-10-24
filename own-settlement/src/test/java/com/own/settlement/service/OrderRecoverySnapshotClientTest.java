package com.own.settlement.service;

import com.own.face.trade.OrderRecoverySnapshot;
import com.own.face.trade.TradeException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderRecoverySnapshotClientTest {
    @Test
    @SuppressWarnings("rawtypes")
    void readsOnlyAuthenticatedAuthoritativeOrderProjection() {
        RestTemplate rest = response(Map.of("status", 200, "data", row("ORD-1", "PAID", "10.00")));
        OrderRecoverySnapshot snapshot = new OrderClient(rest, "internal-test-token").recoverySnapshot("ORD-1");
        assertEquals(7L, snapshot.buyerId()); assertEquals(new BigDecimal("10.00"), snapshot.payableAmount());
        assertTrue(snapshot.paymentCompleted()); assertFalse(snapshot.refundCompleted());
        ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq("http://own-order/api/v1/internal/orders/ORD-1/recovery-snapshot"), eq(HttpMethod.GET), request.capture(), eq(Map.class));
        assertEquals("SYSTEM", request.getValue().getHeaders().getFirst("X-Actor-Type"));
        assertEquals("internal-test-token", request.getValue().getHeaders().getFirst("X-Internal-Service-Token"));
    }

    @Test
    void rejectsFailureWrongOrderAndMalformedData() {
        for (Map<String, ?> body : List.of(Map.of("status", 409, "data", row("ORD-1", "PAID", "10.00")),
                Map.of("status", 200, "data", row("ORD-2", "PAID", "10.00")),
                Map.of("status", 200, "data", row("ORD-1", "PAID", "bad")),
                Map.of("status", 200, "data", Map.of("orderNo", "ORD-1")))) {
            assertThrows(TradeException.class, () -> new OrderClient(response(body), "token").recoverySnapshot("ORD-1"));
        }
    }

    @Test
    void refundedOrderStillProvesAnEarlierCompletedPaymentButPendingOrderDoesNot() {
        assertTrue(new OrderRecoverySnapshot("ORD-1", 7L, BigDecimal.TEN, "REFUNDING", "COMPLETED").paymentCompleted());
        assertTrue(new OrderRecoverySnapshot("ORD-1", 7L, BigDecimal.TEN, "REFUNDED", "COMPLETED").refundCompleted());
        assertFalse(new OrderRecoverySnapshot("ORD-1", 7L, BigDecimal.TEN, "PENDING_PAYMENT", "PENDING").paymentCompleted());
    }

    private Map<String, Object> row(String order, String status, String amount) {
        return Map.of("orderNo", order, "buyerId", 7, "payableAmount", amount, "status", status, "paymentRecoveryStatus", "COMPLETED");
    }
    @SuppressWarnings({"rawtypes", "unchecked"})
    private RestTemplate response(Map body) {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class))).thenReturn(ResponseEntity.ok(body));
        return rest;
    }
}
