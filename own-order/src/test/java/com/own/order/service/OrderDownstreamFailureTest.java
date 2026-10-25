package com.own.order.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.order.domain.OrderStatus;
import com.own.order.domain.TradeOrder;
import com.own.order.repository.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderDownstreamFailureTest {
    private final TradeOrderRepository orders = mock(TradeOrderRepository.class);
    private final OrderEventRepository events = mock(OrderEventRepository.class);
    private final TradeActor system = new TradeActor(1L, ActorType.SYSTEM);

    @Test
    void rejectedInventoryCommitDoesNotConsumeCouponOrMarkOrderPaid() {
        TradeOrder order = order();
        CouponClient coupons = mock(CouponClient.class);
        OrderService service = service(new InventoryClient(failedResponse(), "token", "http://own-inventory", 15), coupons);

        assertEquals(409, assertThrows(TradeException.class, () -> service.paymentSucceeded("ORD-1", system)).getStatus());

        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
        verify(coupons, never()).consume(anyString(), any(BigDecimal.class));
        verify(orders, never()).save(any(TradeOrder.class));
        verifyNoInteractions(events);
    }

    @Test
    void rejectedCouponConsumptionDoesNotMarkOrderPaidAfterInventoryCall() {
        TradeOrder order = order();
        InventoryClient inventory = mock(InventoryClient.class);
        OrderService service = service(inventory, new CouponClient(failedResponse(), "token", "http://own-promotion", 15));

        assertEquals(409, assertThrows(TradeException.class, () -> service.paymentSucceeded("ORD-1", system)).getStatus());

        verify(inventory).commit(eq("ORD-1"), anyList());
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
        verify(orders, never()).save(any(TradeOrder.class));
        verifyNoInteractions(events);
        // This does not roll back the earlier remote inventory effect; C2 still needs durable recovery.
    }

    @Test
    void rejectedInventoryRefundDoesNotReleaseCouponsOrMarkOrderRefunded() {
        TradeOrder order = refunding();
        CouponClient coupons = mock(CouponClient.class);
        OrderService service = service(new InventoryClient(failedResponse(), "token", "http://own-inventory", 15), coupons);

        assertThrows(TradeException.class, () -> service.refundSucceeded("ORD-1", system));

        assertEquals(OrderStatus.REFUNDING, order.getStatus());
        verify(coupons, never()).release(anyString());
        verify(orders, never()).save(any(TradeOrder.class));
        verifyNoInteractions(events);
    }

    @Test
    void rejectedCouponReleaseDoesNotMarkOrderRefundedAfterInventoryCall() {
        TradeOrder order = refunding();
        InventoryClient inventory = mock(InventoryClient.class);
        OrderService service = service(inventory, new CouponClient(failedResponse(), "token", "http://own-promotion", 15));

        assertThrows(TradeException.class, () -> service.refundSucceeded("ORD-1", system));

        verify(inventory).refund(eq("ORD-1"), anyList());
        assertEquals(OrderStatus.REFUNDING, order.getStatus());
        verify(orders, never()).save(any(TradeOrder.class));
        verifyNoInteractions(events);
    }

    private TradeOrder order() {
        TradeOrder order = new TradeOrder("ORD-1", 7L, BigDecimal.TEN, BigDecimal.ONE, "test address", "test-key");
        when(orders.findLockedByOrderNo("ORD-1")).thenReturn(order);
        return order;
    }

    private TradeOrder refunding() {
        TradeOrder order = order();
        order.markPaid(); order.startRefund();
        return order;
    }

    private OrderService service(InventoryClient inventory, CouponClient coupons) {
        OrderItemRepository items = mock(OrderItemRepository.class);
        when(items.findByOrderNo("ORD-1")).thenReturn(List.of());
        OrderService service = new OrderService(mock(CartItemRepository.class), mock(CartBuyerGuardRepository.class), orders,
                mock(TradeSubOrderRepository.class), items, events, mock(CatalogClient.class), inventory, coupons,
                mock(AddressClient.class), mock(MerchantFreightRuleRepository.class), mock(LogisticsTraceRepository.class),
                mock(AfterSaleRepository.class), mock(SensitiveAccessAuditRepository.class), 15);
        OrderPaymentGuard paymentGuard = mock(OrderPaymentGuard.class);
        when(paymentGuard.claim(anyString())).thenReturn(true);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "paymentGuard", paymentGuard);
        return service;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private RestTemplate failedResponse() {
        RestTemplate rest = mock(RestTemplate.class);
        when(rest.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("status", 409, "data", "ORD-1")));
        return rest;
    }
}
