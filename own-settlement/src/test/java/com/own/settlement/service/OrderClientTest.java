package com.own.settlement.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.own.face.trade.TradeException;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

class OrderClientTest {
    private static final String URL = "http://own-order/api/v1/internal/orders/ORD-1/payable-amount?buyerId=7";

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void requestsBuyerScopedAmountThroughTheInternalGuard() {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.exchange(eq(URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<Map>(Map.of("status", 200, "data", "17.90"), HttpStatus.OK));

        BigDecimal amount = new OrderClient(rest, "internal-test-token", "http://own-order").payableAmount("ORD-1", 7L);

        assertEquals(new BigDecimal("17.90"), amount);
        ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq(URL), eq(HttpMethod.GET), request.capture(), eq(Map.class));
        assertEquals("SYSTEM", request.getValue().getHeaders().getFirst("X-Actor-Type"));
        assertEquals("internal-test-token", request.getValue().getHeaders().getFirst("X-Internal-Service-Token"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void missingInternalAmountCannotCreatePayment() {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.exchange(eq(URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<Map>(Map.of("status", 200), HttpStatus.OK));

        TradeException error = assertThrows(TradeException.class,
                () -> new OrderClient(rest, "internal-test-token", "http://own-order").payableAmount("ORD-1", 7L));
        assertEquals(404, error.getStatus());
    }

    @Test
    @SuppressWarnings("rawtypes")
    void orderEligibilityConflictIsNotReportedAsServiceOutage() {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.exchange(eq(URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));

        TradeException error = assertThrows(TradeException.class,
                () -> new OrderClient(rest, "internal-test-token", "http://own-order").payableAmount("ORD-1", 7L));

        assertEquals(409, error.getStatus());
        assertEquals("order is not awaiting payment", error.getMessage());
    }
}
