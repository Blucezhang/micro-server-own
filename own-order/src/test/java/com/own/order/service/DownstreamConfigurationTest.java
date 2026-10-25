package com.own.order.service;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DownstreamConfigurationTest {
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void injectedInventoryOriginAndTtlAreUsedInTheActualRequest() {
        RestTemplate rest = mock(RestTemplate.class);
        String endpoint = "http://inventory-fixture:6011/api/v1/internal/reservations";
        when(rest.exchange(eq(endpoint), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("status", 200)));
        new ApplicationContextRunner().withBean(RestTemplate.class, () -> rest).withBean(InventoryClient.class)
                .withPropertyValues("trade.internal.service-token=fixture-token",
                        "trade.services.inventory-base-url=http://inventory-fixture:6011/",
                        "trade.saga.reservation-ttl-minutes=7")
                .run(context -> {
                    assertNull(context.getStartupFailure());
                    context.getBean(InventoryClient.class).reserve("ORD-CONFIG", 101L, 20L, 2);
                });
        ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq(endpoint), eq(HttpMethod.POST), request.capture(), eq(Map.class));
        assertEquals(7, ((Map<?, ?>) request.getValue().getBody()).get("ttlMinutes"));
        assertEquals("fixture-token", request.getValue().getHeaders().getFirst("X-Internal-Service-Token"));
        assertEquals("order-inventory-reserve-ORD-CONFIG-101-20", request.getValue().getHeaders().getFirst("Idempotency-Key"));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void couponOriginAndReservationLifetimeAreNotCopiedFromInventoryLiterals() {
        RestTemplate rest = mock(RestTemplate.class);
        String endpoint = "http://promotion-fixture/sale/api/v1/coupons/internal/reservations";
        when(rest.exchange(eq(endpoint), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("status", 200, "data", Map.of("totalDiscount", "0", "merchantDiscounts", Map.of()))));
        CouponClient client = new CouponClient(rest, "fixture-token", "http://promotion-fixture", 9);
        var coupon = new com.own.order.dto.CouponUse();
        coupon.setCouponNo("COUPON-CONFIG");
        coupon.setMerchantId(20L);
        coupon.setApplicableAmount(java.math.BigDecimal.TEN);
        client.reserve("ORD-CONFIG", 1L, java.math.BigDecimal.TEN, Map.of(20L, java.math.BigDecimal.TEN), java.util.List.of(coupon));
        ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq(endpoint), eq(HttpMethod.POST), request.capture(), eq(Map.class));
        assertEquals(9, ((Map<?, ?>) request.getValue().getBody()).get("ttlMinutes"));
    }

    @Test
    @SuppressWarnings("rawtypes")
    void catalogReadRetainsItsExistingPathUnderACustomOrigin() {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.getForEntity("http://catalog-fixture/product/Product/{productId}", Map.class, 101L))
                .thenReturn(ResponseEntity.ok(Map.of("data", Map.of("partyId", 20L, "name", "fixture", "originalPrice", "12.50"))));
        var product = new CatalogClient(rest, "http://catalog-fixture/").getProduct(101L);
        assertEquals(new java.math.BigDecimal("12.50"), product.getPrice());
        verify(rest).getForEntity("http://catalog-fixture/product/Product/{productId}", Map.class, 101L);
    }

    @Test
    void invalidReservationLifetimesAreRejectedBeforeAnyRemoteWrite() {
        RestTemplate rest = mock(RestTemplate.class);
        assertThrows(IllegalArgumentException.class, () -> new InventoryClient(rest, "", "http://inventory-fixture", 0));
        assertThrows(IllegalArgumentException.class, () -> new CouponClient(rest, "", "http://promotion-fixture", -1));
        assertThrows(IllegalArgumentException.class, () -> new InventoryClient(rest, "", "http://inventory-fixture", 61));
        assertThrows(IllegalArgumentException.class, () -> new CouponClient(rest, "", "http://promotion-fixture", 61));
        verifyNoInteractions(rest);
    }
}
