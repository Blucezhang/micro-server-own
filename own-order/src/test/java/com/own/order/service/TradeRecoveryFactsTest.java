package com.own.order.service;

import com.own.face.trade.TradeException;
import com.own.order.domain.CartItem;
import com.own.order.domain.OrderItem;
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

class TradeRecoveryFactsTest {
    private final List<OrderItem> items = List.of(new OrderItem("ORD-1", "SUB-1",
            new CartItem(7L, 101L, 20L, "product", BigDecimal.TEN, 2)));

    @Test
    void alreadyCommittedInventoryDoesNotRepeatUnknownHttpWrite() {
        RestTemplate rest = facts(List.of(stock("COMMITTED", 20, 2)));
        new InventoryClient(rest, "token", "http://own-inventory", 15).reconcileCommit("ORD-1", items);
        noWrites(rest);
    }

    @Test
    void alreadyRefundedInventoryDoesNotRepeatRefund() {
        RestTemplate rest = facts(List.of(stock("REFUNDED", 20, 2)));
        new InventoryClient(rest, "token", "http://own-inventory", 15).refund("ORD-1", items);
        noWrites(rest);
    }

    @Test
    void releasedOrExpiredInventoryDoesNotRepeatUnknownRelease() {
        for (String status : List.of("RELEASED", "EXPIRED")) {
            RestTemplate rest = facts(List.of(stock(status, 20, 2)));
            new InventoryClient(rest, "token", "http://own-inventory", 15).reconcileRelease("ORD-1", items);
            noWrites(rest);
        }
    }

    @Test
    void reservedInventoryReleaseRetainsOriginalKey() {
        RestTemplate rest = facts(List.of(stock("RESERVED", 20, 2)));
        new InventoryClient(rest, "token", "http://own-inventory", 15).reconcileRelease("ORD-1", items);
        assertKey(rest, "order-inventory-release-ORD-1");
    }

    @Test
    void committedMissingOrMismatchedInventoryCannotAuthorizeCancellation() {
        for (List<?> rows : List.of(List.of(), List.of(stock("COMMITTED", 20, 2)),
                List.of(stock("RELEASED", 21, 2)), List.of(stock("EXPIRED", 20, 3)))) {
            RestTemplate rest = facts(rows);
            assertThrows(TradeException.class, () -> new InventoryClient(rest, "token", "http://own-inventory", 15).reconcileRelease("ORD-1", items));
            noWrites(rest);
        }
    }

    @Test
    void reservedInventoryUsesOriginalCommitKey() {
        RestTemplate rest = facts(List.of(stock("RESERVED", 20, 2)));
        new InventoryClient(rest, "token", "http://own-inventory", 15).reconcileCommit("ORD-1", items);
        assertKey(rest, "order-inventory-commit-ORD-1");
    }

    @Test
    void committedInventoryUsesOriginalRefundKey() {
        RestTemplate rest = facts(List.of(stock("COMMITTED", 20, 2)));
        new InventoryClient(rest, "token", "http://own-inventory", 15).refund("ORD-1", items);
        assertKey(rest, "order-inventory-refund-ORD-1");
    }

    @Test
    void inventoryMissingWrongOwnerWrongQuantityAndExpiredCannotBeGuessedSuccessful() {
        for (List<?> rows : List.of(List.of(), List.of(stock("COMMITTED", 99, 2)),
                List.of(stock("COMMITTED", 20, 3)), List.of(stock("EXPIRED", 20, 2)),
                List.of(stock("COMMITTED", 20, 2), stock("COMMITTED", 20, 2)))) {
            RestTemplate rest = facts(rows);
            assertThrows(TradeException.class, () -> new InventoryClient(rest, "token", "http://own-inventory", 15).reconcileCommit("ORD-1", items));
            noWrites(rest);
        }
    }

    @Test
    void usedCouponFactsSkipRepeatedConsumption() {
        RestTemplate rest = facts(List.of(coupon("USED", 7, "1.00")));
        new CouponClient(rest, "token", "http://own-promotion", 15).reconcileConsume("ORD-1", 7L, BigDecimal.ONE);
        noWrites(rest);
    }

    @Test
    void reservedCouponFactsRetainOriginalConsumptionBusinessKey() {
        RestTemplate rest = facts(List.of(coupon("RESERVED", 7, "1.00")));
        new CouponClient(rest, "token", "http://own-promotion", 15).reconcileConsume("ORD-1", 7L, BigDecimal.ONE);
        assertKey(rest, "order-coupon-consume-ORD-1");
    }

    @Test
    void couponWrongBuyerAmountDuplicateAndMissingFactsCannotAuthorizeOrderCompletion() {
        for (List<?> rows : List.of(List.of(), List.of(coupon("USED", 8, "1.00")),
                List.of(coupon("USED", 7, "2.00")), List.of(coupon("AVAILABLE", 7, "1.00")),
                List.of(coupon("USED", 7, "0.50"), coupon("USED", 7, "0.50")))) {
            RestTemplate rest = facts(rows);
            assertThrows(TradeException.class, () -> new CouponClient(rest, "token", "http://own-promotion", 15).reconcileConsume("ORD-1", 7L, BigDecimal.ONE));
            noWrites(rest);
        }
    }

    @Test
    void zeroDiscountWithNoCouponsNeedsNoWrite() {
        RestTemplate rest = facts(List.of());
        new CouponClient(rest, "token", "http://own-promotion", 15).reconcileConsume("ORD-1", 7L, BigDecimal.ZERO);
        noWrites(rest);
    }

    private Map<String, Object> stock(String status, int merchant, int quantity) {
        return Map.of("orderNo", "ORD-1", "productId", 101, "merchantId", merchant, "quantity", quantity, "status", status);
    }
    private Map<String, Object> coupon(String status, int buyer, String amount) {
        return Map.of("orderNo", "ORD-1", "couponNo", "CPN-1", "buyerId", buyer, "discountAmount", amount, "status", status);
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private RestTemplate facts(List<?> rows) {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("status", 200, "data", rows)));
        when(rest.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("status", 200, "data", "ORD-1")));
        return rest;
    }
    @SuppressWarnings("rawtypes")
    private void noWrites(RestTemplate rest) {
        verify(rest, never()).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
    }
    @SuppressWarnings("rawtypes")
    private void assertKey(RestTemplate rest, String expected) {
        ArgumentCaptor<HttpEntity> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(rest).exchange(anyString(), eq(HttpMethod.POST), request.capture(), eq(Map.class));
        assertEquals(expected, request.getValue().getHeaders().getFirst("Idempotency-Key"));
    }
}
