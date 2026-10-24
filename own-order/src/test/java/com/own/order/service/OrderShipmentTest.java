package com.own.order.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.order.domain.OrderStatus;
import com.own.order.domain.SubOrderStatus;
import com.own.order.domain.TradeOrder;
import com.own.order.domain.TradeSubOrder;
import com.own.order.dto.ShipmentCommand;
import com.own.order.repository.AfterSaleRepository;
import com.own.order.repository.CartBuyerGuardRepository;
import com.own.order.repository.CartItemRepository;
import com.own.order.repository.LogisticsTraceRepository;
import com.own.order.repository.MerchantFreightRuleRepository;
import com.own.order.repository.OrderEventRepository;
import com.own.order.repository.OrderItemRepository;
import com.own.order.repository.SensitiveAccessAuditRepository;
import com.own.order.repository.TradeOrderRepository;
import com.own.order.repository.TradeSubOrderRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderShipmentTest {
    @Test
    void parentCompletesOnlyAfterBothMerchantsShipAndBuyerReceivesBothSubOrders() {
        TradeOrder parent = new TradeOrder("ORD-1", 1L, new BigDecimal("2.00"),
                BigDecimal.ZERO, "address", "key-1");
        parent.markPaid();
        TradeSubOrder first = toShip("SUB-1", 10L);
        TradeSubOrder second = toShip("SUB-2", 20L);
        TradeOrderRepository orders = mock(TradeOrderRepository.class);
        TradeSubOrderRepository subOrders = mock(TradeSubOrderRepository.class);
        when(orders.findLockedByOrderNo("ORD-1")).thenReturn(parent);
        when(subOrders.findBySubOrderNo("SUB-1")).thenReturn(first);
        when(subOrders.findBySubOrderNo("SUB-2")).thenReturn(second);
        when(subOrders.findBySubOrderNoForUpdate("SUB-1")).thenReturn(first);
        when(subOrders.findBySubOrderNoForUpdate("SUB-2")).thenReturn(second);
        when(subOrders.findByOrderNo("ORD-1")).thenReturn(java.util.Arrays.asList(first, second));
        OrderService service = service(orders, subOrders);
        service.ship(new TradeActor(10L, ActorType.MERCHANT), "SUB-1", shipment("TRACK-1"));
        service.ship(new TradeActor(20L, ActorType.MERCHANT), "SUB-2", shipment("TRACK-2"));

        service.receive(new TradeActor(1L, ActorType.BUYER), "SUB-1");
        assertEquals(OrderStatus.FULFILLING, parent.getStatus());
        assertEquals(SubOrderStatus.RECEIVED, first.getStatus());
        assertEquals(SubOrderStatus.SHIPPED, second.getStatus());

        service.receive(new TradeActor(1L, ActorType.BUYER), "SUB-2");
        assertEquals(OrderStatus.COMPLETED, parent.getStatus());
        assertEquals(409, assertThrows(TradeException.class,
                () -> service.receive(new TradeActor(1L, ActorType.BUYER), "SUB-2")).getStatus());
        verify(orders, times(2)).save(parent);
        verify(subOrders, times(2)).save(second);
    }

    @Test
    void secondMerchantCanShipAfterFirstMerchantStartsFulfillment() {
        TradeOrder parent = paidOrder();
        TradeSubOrder first = toShip("SUB-1", 10L);
        TradeSubOrder second = toShip("SUB-2", 20L);
        TradeOrderRepository orders = mock(TradeOrderRepository.class);
        TradeSubOrderRepository subOrders = mock(TradeSubOrderRepository.class);
        when(orders.findLockedByOrderNo("ORD-1")).thenReturn(parent);
        when(subOrders.findBySubOrderNo("SUB-1")).thenReturn(first);
        when(subOrders.findBySubOrderNo("SUB-2")).thenReturn(second);
        when(subOrders.findBySubOrderNoForUpdate("SUB-1")).thenReturn(first);
        when(subOrders.findBySubOrderNoForUpdate("SUB-2")).thenReturn(second);
        OrderService service = service(orders, subOrders);

        service.ship(new TradeActor(10L, ActorType.MERCHANT), "SUB-1", shipment("TRACK-1"));
        service.ship(new TradeActor(20L, ActorType.MERCHANT), "SUB-2", shipment("TRACK-2"));

        assertEquals(OrderStatus.FULFILLING, parent.getStatus());
        assertEquals(SubOrderStatus.SHIPPED, first.getStatus());
        assertEquals(SubOrderStatus.SHIPPED, second.getStatus());
    }

    @Test
    void shippedSubOrderCannotBeShippedTwice() {
        TradeOrder parent = paidOrder();
        TradeSubOrder subOrder = toShip("SUB-1", 10L);
        TradeOrderRepository orders = mock(TradeOrderRepository.class);
        TradeSubOrderRepository subOrders = mock(TradeSubOrderRepository.class);
        when(orders.findLockedByOrderNo("ORD-1")).thenReturn(parent);
        when(subOrders.findBySubOrderNo("SUB-1")).thenReturn(subOrder);
        when(subOrders.findBySubOrderNoForUpdate("SUB-1")).thenReturn(subOrder);
        OrderService service = service(orders, subOrders);
        service.ship(new TradeActor(10L, ActorType.MERCHANT), "SUB-1", shipment("TRACK-1"));

        TradeException error = assertThrows(TradeException.class,
                () -> service.ship(new TradeActor(10L, ActorType.MERCHANT), "SUB-1", shipment("TRACK-2")));

        assertEquals(409, error.getStatus());
        assertEquals("TRACK-1", subOrder.getTrackingNo());
        verify(subOrders).save(subOrder);
    }

    @Test
    void refundingParentCannotShipRemainingSubOrder() {
        TradeOrder parent = paidOrder();
        parent.startRefund();
        TradeSubOrder subOrder = toShip("SUB-1", 10L);
        TradeOrderRepository orders = mock(TradeOrderRepository.class);
        TradeSubOrderRepository subOrders = mock(TradeSubOrderRepository.class);
        when(orders.findLockedByOrderNo("ORD-1")).thenReturn(parent);
        when(subOrders.findBySubOrderNo("SUB-1")).thenReturn(subOrder);
        when(subOrders.findBySubOrderNoForUpdate("SUB-1")).thenReturn(subOrder);

        TradeException error = assertThrows(TradeException.class,
                () -> service(orders, subOrders).ship(new TradeActor(10L, ActorType.MERCHANT),
                        "SUB-1", shipment("TRACK-1")));

        assertEquals(409, error.getStatus());
        assertEquals(SubOrderStatus.TO_SHIP, subOrder.getStatus());
        verify(subOrders, never()).save(subOrder);
    }

    @Test
    void anotherMerchantCannotShipThisSubOrder() {
        TradeSubOrder subOrder = toShip("SUB-1", 10L);
        TradeOrderRepository orders = mock(TradeOrderRepository.class);
        TradeSubOrderRepository subOrders = mock(TradeSubOrderRepository.class);
        when(subOrders.findBySubOrderNo("SUB-1")).thenReturn(subOrder);

        TradeException error = assertThrows(TradeException.class,
                () -> service(orders, subOrders).ship(new TradeActor(20L, ActorType.MERCHANT),
                        "SUB-1", shipment("TRACK-1")));

        assertEquals(403, error.getStatus());
        verify(orders, never()).findLockedByOrderNo("ORD-1");
    }

    private TradeOrder paidOrder() {
        TradeOrder order = new TradeOrder("ORD-1", 1L, BigDecimal.ONE, BigDecimal.ZERO, "address", "key-1");
        order.markPaid();
        return order;
    }

    private TradeSubOrder toShip(String subOrderNo, Long merchantId) {
        TradeSubOrder subOrder = new TradeSubOrder(subOrderNo, "ORD-1", merchantId,
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);
        subOrder.markToShip();
        return subOrder;
    }

    private ShipmentCommand shipment(String trackingNo) {
        ShipmentCommand command = new ShipmentCommand();
        command.setLogisticsCompany("DEMO");
        command.setTrackingNo(trackingNo);
        return command;
    }

    private OrderService service(TradeOrderRepository orders, TradeSubOrderRepository subOrders) {
        return new OrderService(mock(CartItemRepository.class), mock(CartBuyerGuardRepository.class), orders,
                subOrders, mock(OrderItemRepository.class), mock(OrderEventRepository.class), mock(CatalogClient.class),
                mock(InventoryClient.class), mock(CouponClient.class), mock(AddressClient.class),
                mock(MerchantFreightRuleRepository.class), mock(LogisticsTraceRepository.class),
                mock(AfterSaleRepository.class), mock(SensitiveAccessAuditRepository.class));
    }
}
