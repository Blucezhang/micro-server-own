package com.own.order.service;

import java.util.Collections;
import java.util.Map;
import java.math.BigDecimal;
import com.own.order.dto.CouponUse;
import com.own.order.domain.CartItem;
import com.own.order.domain.OrderItem;
import com.own.face.trade.TradeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DownstreamIdempotencyKeyTest {
    @Test
    @SuppressWarnings("unchecked")
    public void afterSaleRefundCarriesAuthoritativeMerchantAndStableBusinessKey() {
        RestTemplate rest = mock(RestTemplate.class);
        String url = "http://own-settlement/api/v1/payments/internal/after-sales/refund";
        when(rest.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<Map>(Map.of("status", 200), HttpStatus.OK));
        com.own.order.domain.AfterSale sale = new com.own.order.domain.AfterSale("AS-1", "ORD-1", "SUB-1", 7L, 20L,
                com.own.order.domain.AfterSaleType.REFUND_ONLY, new BigDecimal("4.00"), "test refund");
        SettlementClient client = new SettlementClient(rest, "internal-test-token");

        client.refundAfterSale(sale);
        client.refundAfterSale(sale);

        ArgumentCaptor<HttpEntity> requests = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest, times(2)).exchange(eq(url), eq(HttpMethod.POST), requests.capture(), eq(Map.class));
        for (HttpEntity request : requests.getAllValues()) {
            Map body = (Map) request.getBody();
            Assertions.assertEquals(20L, body.get("merchantId"));
            Assertions.assertEquals(7L, body.get("buyerId"));
            Assertions.assertEquals("ORD-1", body.get("orderNo"));
            Assertions.assertEquals("AS-1", body.get("afterSaleNo"));
            Assertions.assertEquals(new BigDecimal("4.00"), body.get("amount"));
            Assertions.assertEquals("order-after-sale-AS-1", request.getHeaders().getFirst("Idempotency-Key"));
            Assertions.assertEquals("internal-test-token", request.getHeaders().getFirst("X-Internal-Service-Token"));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void inventoryRetriesReuseBusinessScopedKey() {
        RestTemplate rest = mock(RestTemplate.class);
        String url = "http://own-inventory/api/v1/internal/orders/ORD-1/inventory/commit";
        when(rest.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(java.util.Map.class)))
                .thenReturn(new ResponseEntity<java.util.Map>(Map.of("status", 200), HttpStatus.OK));
        InventoryClient client = new InventoryClient(rest, "internal-test-token");

        OrderItem item = new OrderItem("ORD-1", "SUB-1",
                new CartItem(1L, 101L, 10L, "test product", BigDecimal.ONE, 2));
        OrderItem duplicateSku = new OrderItem("ORD-1", "SUB-1",
                new CartItem(1L, 101L, 10L, "test product", BigDecimal.ONE, 1));
        client.commit("ORD-1", java.util.Arrays.asList(item, duplicateSku));
        client.commit("ORD-1", java.util.Arrays.asList(item, duplicateSku));

        ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest, times(2)).exchange(eq(url), eq(HttpMethod.POST), request.capture(), eq(java.util.Map.class));
        Assertions.assertEquals("order-inventory-commit-ORD-1", request.getAllValues().get(0).getHeaders().getFirst("Idempotency-Key"));
        Assertions.assertEquals(request.getAllValues().get(0).getHeaders().getFirst("Idempotency-Key"), request.getAllValues().get(1).getHeaders().getFirst("Idempotency-Key"));
        Assertions.assertEquals("internal-test-token", request.getAllValues().get(0).getHeaders().getFirst("X-Internal-Service-Token"));
        java.util.List<?> body = (java.util.List<?>) request.getAllValues().get(0).getBody();
        Assertions.assertEquals(1, body.size());
        Assertions.assertEquals(101L, ((java.util.Map<?, ?>) body.get(0)).get("productId"));
        Assertions.assertEquals(10L, ((java.util.Map<?, ?>) body.get(0)).get("merchantId"));
        Assertions.assertEquals(3, ((java.util.Map<?, ?>) body.get(0)).get("quantity"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void expiredInventoryConflictIsNotReportedAsServiceOutage() {
        RestTemplate rest = mock(RestTemplate.class);
        String url = "http://own-inventory/api/v1/internal/orders/ORD-1/inventory/commit";
        when(rest.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(java.util.Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));
        InventoryClient client = new InventoryClient(rest, "internal-test-token");
        OrderItem item = new OrderItem("ORD-1", "SUB-1",
                new CartItem(1L, 101L, 10L, "test product", BigDecimal.ONE, 1));

        TradeException error = Assertions.assertThrows(TradeException.class,
                () -> client.commit("ORD-1", Collections.singletonList(item)));

        Assertions.assertEquals(409, error.getStatus());
        Assertions.assertEquals("inventory operation conflicts with current state", error.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void inventoryRefundCarriesOrderItemsAndStableKey() {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.exchange(eq("http://own-inventory/api/v1/internal/orders/ORD-1/inventory/state"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<Map>(Map.of("status", 200, "data", java.util.List.of(Map.of(
                        "orderNo", "ORD-1", "productId", 101L, "merchantId", 10L, "quantity", 2, "status", "COMMITTED"))), HttpStatus.OK));
        String url = "http://own-inventory/api/v1/internal/orders/ORD-1/inventory/refund";
        when(rest.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(java.util.Map.class)))
                .thenReturn(new ResponseEntity<java.util.Map>(Map.of("status", 200), HttpStatus.OK));
        InventoryClient client = new InventoryClient(rest, "internal-test-token");
        OrderItem item = new OrderItem("ORD-1", "SUB-1",
                new CartItem(1L, 101L, 10L, "test product", BigDecimal.ONE, 2));

        client.refund("ORD-1", Collections.singletonList(item));

        ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq(url), eq(HttpMethod.POST), request.capture(), eq(java.util.Map.class));
        Assertions.assertEquals("order-inventory-refund-ORD-1", request.getValue().getHeaders().getFirst("Idempotency-Key"));
        java.util.List<?> body = (java.util.List<?>) request.getValue().getBody();
        Assertions.assertEquals(1, body.size());
        Assertions.assertEquals(2, ((java.util.Map<?, ?>) body.get(0)).get("quantity"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void couponRetriesReuseActionScopedKey() {
        RestTemplate rest = mock(RestTemplate.class);
        String url = "http://own-promotion/sale/api/v1/coupons/internal/orders/consume";
        when(rest.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(java.util.Map.class)))
                .thenReturn(new ResponseEntity<java.util.Map>(Map.of("status", 200), HttpStatus.OK));
        CouponClient client = new CouponClient(rest, "internal-test-token");

        client.consume("ORD-1", BigDecimal.ONE);
        client.consume("ORD-1", BigDecimal.ONE);

        ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest, times(2)).exchange(eq(url), eq(HttpMethod.POST), request.capture(), eq(java.util.Map.class));
        Assertions.assertEquals("order-coupon-consume-ORD-1", request.getAllValues().get(0).getHeaders().getFirst("Idempotency-Key"));
        Assertions.assertEquals(request.getAllValues().get(0).getHeaders().getFirst("Idempotency-Key"), request.getAllValues().get(1).getHeaders().getFirst("Idempotency-Key"));
        Assertions.assertEquals("internal-test-token", request.getAllValues().get(0).getHeaders().getFirst("X-Internal-Service-Token"));
        Assertions.assertEquals(BigDecimal.ONE, ((java.util.Map) request.getAllValues().get(0).getBody()).get("expectedDiscount"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void expiredCouponConflictIsNotReportedAsPromotionOutage() {
        RestTemplate rest = mock(RestTemplate.class);
        String url = "http://own-promotion/sale/api/v1/coupons/internal/orders/consume";
        when(rest.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(java.util.Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));

        TradeException error = Assertions.assertThrows(TradeException.class,
                () -> new CouponClient(rest, "internal-test-token").consume("ORD-1", BigDecimal.ONE));

        Assertions.assertEquals(409, error.getStatus());
        Assertions.assertEquals("coupon reservation no longer matches order", error.getMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void oneAggregatedReservationCarriesTheTotalSkuQuantity() {
        RestTemplate rest = mock(RestTemplate.class);
        String url = "http://own-inventory/api/v1/internal/reservations";
        when(rest.exchange(eq(url), eq(HttpMethod.POST), any(HttpEntity.class), eq(java.util.Map.class)))
                .thenReturn(new ResponseEntity<java.util.Map>(Map.of("status", 200), HttpStatus.OK));
        InventoryClient client = new InventoryClient(rest, "internal-test-token");

        client.reserve("ORD-1", 10L, 20L, 3);

        ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq(url), eq(HttpMethod.POST), request.capture(), eq(java.util.Map.class));
        java.util.Map body = (java.util.Map) request.getValue().getBody();
        Assertions.assertEquals(Integer.valueOf(3), body.get("quantity"));
        Assertions.assertEquals("order-inventory-reserve-ORD-1-10-20", request.getValue().getHeaders().getFirst("Idempotency-Key"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void configuredSagaDeadlineIsSentToBothReservationServices() {
        RestTemplate rest = mock(RestTemplate.class);
        String inventoryUrl = "http://own-inventory/api/v1/internal/reservations";
        String couponUrl = "http://own-promotion/sale/api/v1/coupons/internal/reservations";
        when(rest.exchange(eq(inventoryUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<Map>(Map.of("status", 200), HttpStatus.OK));
        when(rest.exchange(eq(couponUrl), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<Map>(Map.of("status", 200, "data", Map.of("totalDiscount", "1.00")), HttpStatus.OK));
        InventoryClient inventory = new InventoryClient(rest, "internal-test-token");
        CouponClient coupons = new CouponClient(rest, "internal-test-token");
        ReflectionTestUtils.setField(inventory, "reservationTtlMinutes", 30);
        ReflectionTestUtils.setField(coupons, "reservationTtlMinutes", 30);
        CouponUse use = new CouponUse();
        use.setCouponNo("CPN-1");
        use.setMerchantId(20L);
        use.setApplicableAmount(BigDecimal.TEN);

        inventory.reserve("ORD-1", 10L, 20L, 3);
        coupons.reserve("ORD-1", 7L, BigDecimal.TEN, Map.of(20L, BigDecimal.TEN), Collections.singletonList(use));

        ArgumentCaptor<HttpEntity> inventoryRequest = ArgumentCaptor.forClass(HttpEntity.class);
        ArgumentCaptor<HttpEntity> couponRequest = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(eq(inventoryUrl), eq(HttpMethod.POST), inventoryRequest.capture(), eq(Map.class));
        verify(rest).exchange(eq(couponUrl), eq(HttpMethod.POST), couponRequest.capture(), eq(Map.class));
        Assertions.assertEquals(30, ((Map) inventoryRequest.getValue().getBody()).get("ttlMinutes"));
        Assertions.assertEquals(30, ((Map) couponRequest.getValue().getBody()).get("ttlMinutes"));
    }
}
