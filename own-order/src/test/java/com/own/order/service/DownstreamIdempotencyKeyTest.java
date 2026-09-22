package com.own.order.service;

import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DownstreamIdempotencyKeyTest {
    @Test
    @SuppressWarnings("unchecked")
    public void inventoryRetriesReuseBusinessScopedKey() {
        RestTemplate rest = mock(RestTemplate.class);
        String url = "http://own-inventory/api/v1/internal/orders/ORD-1/inventory/commit";
        when(rest.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(java.util.Map.class)))
                .thenReturn(new ResponseEntity<java.util.Map>(Collections.emptyMap(), HttpStatus.OK));
        InventoryClient client = new InventoryClient(rest, "internal-test-token");

        client.commit("ORD-1");
        client.commit("ORD-1");

        ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest, times(2)).exchange(eq(url), eq(HttpMethod.POST), request.capture(), eq(java.util.Map.class));
        Assertions.assertEquals("order-inventory-commit-ORD-1", request.getAllValues().get(0).getHeaders().getFirst("Idempotency-Key"));
        Assertions.assertEquals(request.getAllValues().get(0).getHeaders().getFirst("Idempotency-Key"), request.getAllValues().get(1).getHeaders().getFirst("Idempotency-Key"));
        Assertions.assertEquals("internal-test-token", request.getAllValues().get(0).getHeaders().getFirst("X-Internal-Service-Token"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void couponRetriesReuseActionScopedKey() {
        RestTemplate rest = mock(RestTemplate.class);
        String url = "http://own-promotion/sale/api/v1/coupons/internal/orders/consume";
        when(rest.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(java.util.Map.class)))
                .thenReturn(new ResponseEntity<java.util.Map>(Collections.emptyMap(), HttpStatus.OK));
        CouponClient client = new CouponClient(rest, "internal-test-token");

        client.consume("ORD-1");
        client.consume("ORD-1");

        ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest, times(2)).exchange(eq(url), eq(HttpMethod.POST), request.capture(), eq(java.util.Map.class));
        Assertions.assertEquals("order-coupon-consume-ORD-1", request.getAllValues().get(0).getHeaders().getFirst("Idempotency-Key"));
        Assertions.assertEquals(request.getAllValues().get(0).getHeaders().getFirst("Idempotency-Key"), request.getAllValues().get(1).getHeaders().getFirst("Idempotency-Key"));
        Assertions.assertEquals("internal-test-token", request.getAllValues().get(0).getHeaders().getFirst("X-Internal-Service-Token"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void oneAggregatedReservationCarriesTheTotalSkuQuantity() {
        RestTemplate rest = mock(RestTemplate.class);
        String url = "http://own-inventory/api/v1/internal/reservations";
        when(rest.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(java.util.Map.class)))
                .thenReturn(new ResponseEntity<java.util.Map>(Collections.emptyMap(), HttpStatus.OK));
        InventoryClient client = new InventoryClient(rest, "internal-test-token");

        client.reserve("ORD-1", 10L, 20L, 3);

        ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq(url), eq(HttpMethod.POST), request.capture(), eq(java.util.Map.class));
        java.util.Map body = (java.util.Map) request.getValue().getBody();
        Assertions.assertEquals(Integer.valueOf(3), body.get("quantity"));
        Assertions.assertEquals("order-inventory-reserve-ORD-1-10-20", request.getValue().getHeaders().getFirst("Idempotency-Key"));
    }
}
