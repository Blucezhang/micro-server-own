package com.own.order.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.order.domain.TradeOrder;
import com.own.order.domain.TradeSubOrder;
import com.own.order.domain.OrderEvent;
import com.own.order.domain.CartItem;
import com.own.order.dto.UpdateCartItemCommand;
import com.own.order.repository.CartItemRepository;
import com.own.order.repository.OrderEventRepository;
import com.own.order.repository.OrderItemRepository;
import com.own.order.repository.SensitiveAccessAuditRepository;
import com.own.order.repository.TradeOrderRepository;
import com.own.order.repository.TradeSubOrderRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class OrderPaymentCallbackTest {
    @Test
    public void expiredOrderRejectsLatePaymentCallback() {
        TradeOrder order = new TradeOrder("ORD-1", 1L, BigDecimal.ONE, BigDecimal.ZERO, "address", "key-1");
        order.cancel();
        TradeOrderRepository orders = Mockito.mock(TradeOrderRepository.class);
        InventoryClient inventory = Mockito.mock(InventoryClient.class);
        CouponClient coupons = Mockito.mock(CouponClient.class);
        Mockito.when(orders.findLockedByOrderNo("ORD-1")).thenReturn(order);
        OrderService service = service(orders, inventory, coupons);

        try {
            service.paymentSucceeded("ORD-1", new TradeActor(1L, ActorType.SYSTEM));
            Assert.fail("late payment must not revive a canceled order");
        } catch (TradeException expected) {
            Assert.assertEquals(409, expected.getStatus());
        }
        Mockito.verify(inventory, Mockito.never()).commit("ORD-1");
        Mockito.verify(coupons, Mockito.never()).consume("ORD-1");
    }

    @Test
    public void paidOrderAcceptsIdempotentPaymentCallback() {
        TradeOrder order = new TradeOrder("ORD-1", 1L, BigDecimal.ONE, BigDecimal.ZERO, "address", "key-1");
        order.markPaid();
        TradeOrderRepository orders = Mockito.mock(TradeOrderRepository.class);
        Mockito.when(orders.findLockedByOrderNo("ORD-1")).thenReturn(order);
        InventoryClient inventory = Mockito.mock(InventoryClient.class);
        CouponClient coupons = Mockito.mock(CouponClient.class);

        TradeOrder result = service(orders, inventory, coupons)
                .paymentSucceeded("ORD-1", new TradeActor(1L, ActorType.SYSTEM));

        Assert.assertSame(order, result);
        Mockito.verify(inventory, Mockito.never()).commit("ORD-1");
        Mockito.verify(coupons, Mockito.never()).consume("ORD-1");
    }

    @Test
    public void buyerOrderHistoryIsPagedAndBoundToBuyer() {
        TradeOrderRepository orders = Mockito.mock(TradeOrderRepository.class);
        TradeOrder order = new TradeOrder("ORD-2", 7L, BigDecimal.ONE, BigDecimal.ZERO, "address", "key-2");
        Mockito.when(orders.pageByBuyer(Mockito.eq(7L), Mockito.isNull(com.own.order.domain.OrderStatus.class), Mockito.isNull(java.util.Date.class), Mockito.isNull(java.util.Date.class), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<TradeOrder>(Arrays.asList(order)));
        Map<String, Object> result = service(orders, Mockito.mock(InventoryClient.class), Mockito.mock(CouponClient.class))
                .buyerOrderPage(new TradeActor(7L, ActorType.BUYER), null, null, null, 0, 20);
        Assert.assertEquals(1L, result.get("total"));
        Assert.assertEquals(1, ((java.util.List) result.get("items")).size());
    }

    @Test
    public void merchantDetailUsesOnlyItsSubOrderSafeTimeline() {
        TradeOrder order = new TradeOrder("ORD-3", 7L, BigDecimal.ONE, BigDecimal.ZERO, "address", "key-3");
        TradeSubOrder subOrder = new TradeSubOrder("SUB-3", "ORD-3", 8L, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);
        TradeOrderRepository orders = Mockito.mock(TradeOrderRepository.class);
        TradeSubOrderRepository subOrders = Mockito.mock(TradeSubOrderRepository.class);
        OrderEventRepository events = Mockito.mock(OrderEventRepository.class);
        Mockito.when(subOrders.findBySubOrderNo("SUB-3")).thenReturn(subOrder);
        Mockito.when(orders.findByOrderNo("ORD-3")).thenReturn(order);
        Mockito.when(events.findBySubOrderNoOrderByIdAsc("SUB-3")).thenReturn(Collections.singletonList(new OrderEvent("ORD-3", "SUB-3", "SUB_ORDER_SHIPPED", 8L, "MERCHANT", "tracking-private")));
        OrderService service = new OrderService(Mockito.mock(CartItemRepository.class), guard(), orders, subOrders,
                Mockito.mock(OrderItemRepository.class), events, Mockito.mock(CatalogClient.class), Mockito.mock(InventoryClient.class),
                Mockito.mock(CouponClient.class), Mockito.mock(AddressClient.class), Mockito.mock(com.own.order.repository.MerchantFreightRuleRepository.class),
                Mockito.mock(com.own.order.repository.LogisticsTraceRepository.class), Mockito.mock(com.own.order.repository.AfterSaleRepository.class), Mockito.mock(SensitiveAccessAuditRepository.class));

        Map<String, Object> detail = service.merchantSubOrderDetail(new TradeActor(8L, ActorType.MERCHANT), "SUB-3");

        java.util.List<com.own.order.dto.OrderTimelineEvent> timeline = (java.util.List<com.own.order.dto.OrderTimelineEvent>) detail.get("events");
        Assert.assertEquals(1, timeline.size());
        Assert.assertEquals("SUB_ORDER_SHIPPED", timeline.get(0).getEventType());
    }

    @Test
    public void buyerCanUpdateOnlyOwnCartItemAfterCatalogRevalidation() {
        CartItem item = new CartItem(7L, 101L, 8L, "product", new BigDecimal("9.90"), 1);
        CartItemRepository carts = Mockito.mock(CartItemRepository.class);
        CatalogClient catalog = Mockito.mock(CatalogClient.class);
        Mockito.when(carts.findByIdAndBuyerId(5L, 7L)).thenReturn(item);
        Mockito.when(catalog.getProduct(101L)).thenReturn(catalogProduct(101L, 8L, "product", new BigDecimal("9.90")));
        Mockito.when(carts.save(item)).thenReturn(item);
        OrderService service = new OrderService(carts, guard(), Mockito.mock(TradeOrderRepository.class), Mockito.mock(TradeSubOrderRepository.class),
                Mockito.mock(OrderItemRepository.class), Mockito.mock(OrderEventRepository.class), catalog, Mockito.mock(InventoryClient.class),
                Mockito.mock(CouponClient.class), Mockito.mock(AddressClient.class), Mockito.mock(com.own.order.repository.MerchantFreightRuleRepository.class),
                Mockito.mock(com.own.order.repository.LogisticsTraceRepository.class), Mockito.mock(com.own.order.repository.AfterSaleRepository.class), Mockito.mock(SensitiveAccessAuditRepository.class));
        UpdateCartItemCommand command = new UpdateCartItemCommand(); command.setQuantity(3);

        CartItem updated = service.updateCartItem(new TradeActor(7L, ActorType.BUYER), 5L, command);

        Assert.assertEquals(Integer.valueOf(3), updated.getQuantity());
    }

    @Test
    public void addingSameSkuMergesTheBuyerCartLine() {
        CartItem existing = new CartItem(7L, 101L, 8L, "product", new BigDecimal("9.90"), 2);
        CartItemRepository carts = Mockito.mock(CartItemRepository.class);
        CatalogClient catalog = Mockito.mock(CatalogClient.class);
        Mockito.when(catalog.getProduct(101L)).thenReturn(catalogProduct(101L, 8L, "product", new BigDecimal("9.90")));
        Mockito.when(carts.findByBuyerIdAndProductId(7L, 101L)).thenReturn(existing);
        Mockito.when(carts.save(existing)).thenReturn(existing);
        OrderService service = new OrderService(carts, guard(), Mockito.mock(TradeOrderRepository.class), Mockito.mock(TradeSubOrderRepository.class),
                Mockito.mock(OrderItemRepository.class), Mockito.mock(OrderEventRepository.class), catalog, Mockito.mock(InventoryClient.class),
                Mockito.mock(CouponClient.class), Mockito.mock(AddressClient.class), Mockito.mock(com.own.order.repository.MerchantFreightRuleRepository.class),
                Mockito.mock(com.own.order.repository.LogisticsTraceRepository.class), Mockito.mock(com.own.order.repository.AfterSaleRepository.class), Mockito.mock(SensitiveAccessAuditRepository.class));
        com.own.order.dto.AddCartItemCommand command = new com.own.order.dto.AddCartItemCommand(); command.setProductId(101L); command.setQuantity(3);

        CartItem updated = service.addCartItem(new TradeActor(7L, ActorType.BUYER), command);

        Assert.assertSame(existing, updated);
        Assert.assertEquals(Integer.valueOf(5), updated.getQuantity());
    }

    private CatalogClient.CatalogProduct catalogProduct(Long productId, Long merchantId, String name, BigDecimal price) {
        try {
            java.lang.reflect.Constructor<CatalogClient.CatalogProduct> constructor = CatalogClient.CatalogProduct.class
                    .getDeclaredConstructor(Long.class, Long.class, String.class, BigDecimal.class);
            constructor.setAccessible(true);
            return constructor.newInstance(productId, merchantId, name, price);
        } catch (Exception exception) { throw new AssertionError(exception); }
    }

    private OrderService service(TradeOrderRepository orders, InventoryClient inventory, CouponClient coupons) {
        return new OrderService(Mockito.mock(CartItemRepository.class), guard(), orders, Mockito.mock(TradeSubOrderRepository.class),
                Mockito.mock(OrderItemRepository.class), Mockito.mock(OrderEventRepository.class), Mockito.mock(CatalogClient.class),
                inventory, coupons, Mockito.mock(AddressClient.class),
                Mockito.mock(com.own.order.repository.MerchantFreightRuleRepository.class),
                Mockito.mock(com.own.order.repository.LogisticsTraceRepository.class),
                Mockito.mock(com.own.order.repository.AfterSaleRepository.class), Mockito.mock(SensitiveAccessAuditRepository.class));
    }

    private com.own.order.repository.CartBuyerGuardRepository guard() {
        com.own.order.repository.CartBuyerGuardRepository guards = Mockito.mock(com.own.order.repository.CartBuyerGuardRepository.class);
        Mockito.when(guards.findByBuyerIdForUpdate(Mockito.anyLong())).thenReturn(Mockito.mock(com.own.order.domain.CartBuyerGuard.class));
        return guards;
    }
}
