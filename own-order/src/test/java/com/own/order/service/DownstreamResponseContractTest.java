package com.own.order.service;

import com.own.face.trade.TradeException;
import com.own.order.domain.AfterSale;
import com.own.order.domain.AfterSaleType;
import com.own.order.domain.CartItem;
import com.own.order.domain.OrderItem;
import com.own.order.dto.CouponUse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DownstreamResponseContractTest {
    @Test
    void inventoryWriteActionsRejectBusinessFailureInsideHttpSuccess() {
        RestTemplate rest = response(Map.of("status", 409, "data", "ORD-1"), HttpStatus.OK);
        InventoryClient client = new InventoryClient(rest, "test-internal-token", "http://own-inventory", 15);
        List<OrderItem> items = List.of(new OrderItem("ORD-1", "SUB-1",
                new CartItem(1L, 101L, 20L, "test", BigDecimal.TEN, 1)));
        assertThrows(TradeException.class, () -> client.reserve("ORD-1", 101L, 20L, 1));
        assertThrows(TradeException.class, () -> client.commit("ORD-1", items));
        assertThrows(TradeException.class, () -> client.release("ORD-1"));
        assertThrows(TradeException.class, () -> client.refund("ORD-1", items));
        assertThrows(TradeException.class, () -> client.refundAfterSale("AS-1", 20L, List.of()));
        assertThrows(TradeException.class, () -> client.exchangeAfterSale("AS-1", 20L, List.of()));
    }

    @Test
    void couponWriteActionsRejectBusinessFailureInsideHttpSuccess() {
        CouponClient client = new CouponClient(response(Map.of("status", 409, "data", "ORD-1"), HttpStatus.OK), "token", "http://own-promotion", 15);
        assertThrows(TradeException.class, () -> client.consume("ORD-1", BigDecimal.ONE));
        assertThrows(TradeException.class, () -> client.release("ORD-1"));
    }

    @Test
    void couponReserveAndQuoteRejectFailedEnvelopesEvenWithPlausibleDiscountData() {
        CouponClient client = new CouponClient(response(Map.of("status", 409,
                "data", Map.of("totalDiscount", "1.00", "merchantDiscounts", Map.of("20", "1.00"))), HttpStatus.OK), "token", "http://own-promotion", 15);
        CouponUse coupon = new CouponUse();
        coupon.setCouponNo("CPN-1"); coupon.setMerchantId(20L);
        assertThrows(TradeException.class,
                () -> client.reserve("ORD-1", 1L, BigDecimal.TEN, Map.of(20L, BigDecimal.TEN), List.of(coupon)));
        assertThrows(TradeException.class,
                () -> client.preview(1L, BigDecimal.TEN, Map.of(20L, BigDecimal.TEN), List.of(coupon)));
    }

    @Test
    void afterSaleDispatchRejectsBusinessFailureInsideHttpSuccess() {
        SettlementClient client = new SettlementClient(response(Map.of("status", 409, "data", "AS-1"), HttpStatus.OK), "token", "http://own-settlement");
        assertThrows(TradeException.class, () -> client.refundAfterSale(sale()));
    }

    @Test
    void missingBusinessStatusIsNotAnAcknowledgement() {
        RestTemplate rest = response(Map.of("data", "ORD-1"), HttpStatus.OK);
        assertThrows(TradeException.class, () -> new InventoryClient(rest, "token", "http://own-inventory", 15).release("ORD-1"));
        assertThrows(TradeException.class, () -> new CouponClient(rest, "token", "http://own-promotion", 15).release("ORD-1"));
        assertThrows(TradeException.class, () -> new SettlementClient(rest, "token", "http://own-settlement").refundAfterSale(sale()));
    }

    @Test
    void nonSuccessfulHttpStatusCannotBeMaskedBySuccessfulBody() {
        RestTemplate rest = response(Map.of("status", 200, "data", "ORD-1"), HttpStatus.SERVICE_UNAVAILABLE);
        assertThrows(TradeException.class, () -> new InventoryClient(rest, "token", "http://own-inventory", 15).release("ORD-1"));
        assertThrows(TradeException.class, () -> new CouponClient(rest, "token", "http://own-promotion", 15).release("ORD-1"));
        assertThrows(TradeException.class, () -> new SettlementClient(rest, "token", "http://own-settlement").refundAfterSale(sale()));
    }

    @Test
    void legitimateCreatedResponseIsStillAccepted() {
        RestTemplate rest = response(Map.of("status", 201, "data", "created"), HttpStatus.CREATED);
        assertDoesNotThrow(() -> new InventoryClient(rest, "token", "http://own-inventory", 15).reserve("ORD-1", 101L, 20L, 1));
        assertDoesNotThrow(() -> new CouponClient(rest, "token", "http://own-promotion", 15).consume("ORD-1", BigDecimal.ONE));
        assertDoesNotThrow(() -> new SettlementClient(rest, "token", "http://own-settlement").refundAfterSale(sale()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private RestTemplate response(Map body, HttpStatus status) {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(new ResponseEntity<Map>(body, status));
        return rest;
    }

    private AfterSale sale() {
        return new AfterSale("AS-1", "ORD-1", "SUB-1", 1L, 20L, AfterSaleType.REFUND_ONLY, BigDecimal.ONE, "test");
    }
}
