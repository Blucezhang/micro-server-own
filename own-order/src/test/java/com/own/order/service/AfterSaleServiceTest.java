package com.own.order.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.order.domain.AfterSale;
import com.own.order.domain.AfterSaleItem;
import com.own.order.domain.CartItem;
import com.own.order.domain.OrderItem;
import com.own.order.domain.SubOrderStatus;
import com.own.order.domain.TradeOrder;
import com.own.order.domain.TradeSubOrder;
import com.own.order.dto.AfterSaleItemCommand;
import com.own.order.dto.AfterSaleAuditCommand;
import com.own.order.dto.AfterSaleDetail;
import com.own.order.dto.CreateAfterSaleCommand;
import com.own.order.dto.ReturnShipmentCommand;
import com.own.order.domain.AfterSaleType;
import com.own.order.repository.AfterSaleItemRepository;
import com.own.order.repository.AfterSaleEvidenceRepository;
import com.own.order.repository.AfterSaleRepository;
import com.own.order.repository.OrderEventRepository;
import com.own.order.repository.OrderItemRepository;
import com.own.order.repository.TradeOrderRepository;
import com.own.order.repository.TradeSubOrderRepository;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

public class AfterSaleServiceTest {
    @Test
    @SuppressWarnings("unchecked") // Mockito 1.x generic matcher API is raw-typed.
    public void duplicateOrderItemCannotBypassPurchasedQuantityLimit() {
        TradeSubOrder subOrder = new TradeSubOrder("SUB-1", "ORD-1", 2L,
                new BigDecimal("10.00"), BigDecimal.ZERO, BigDecimal.ZERO);
        subOrder.markToShip();
        subOrder.ship("DEMO", "TRACK-1");
        Assertions.assertEquals(SubOrderStatus.SHIPPED, subOrder.getStatus());
        TradeOrder order = new TradeOrder("ORD-1", 1L, new BigDecimal("10.00"),
                BigDecimal.ZERO, "history", "key-1");
        OrderItem item = new OrderItem("ORD-1", "SUB-1",
                new CartItem(1L, 101L, 2L, "product", new BigDecimal("10.00"), 1));
        ReflectionTestUtils.setField(item, "id", 11L);

        TradeSubOrderRepository subOrders = Mockito.mock(TradeSubOrderRepository.class);
        TradeOrderRepository orders = Mockito.mock(TradeOrderRepository.class);
        OrderItemRepository items = Mockito.mock(OrderItemRepository.class);
        AfterSaleRepository sales = Mockito.mock(AfterSaleRepository.class);
        Mockito.when(subOrders.findBySubOrderNoForUpdate("SUB-1")).thenReturn(subOrder);
        Mockito.when(orders.findByOrderNo("ORD-1")).thenReturn(order);
        Mockito.when(items.findBySubOrderNo("SUB-1")).thenReturn(Collections.singletonList(item));
        Mockito.when(sales.findBySubOrderNoAndStatusIn(Mockito.eq("SUB-1"), Mockito.<com.own.order.domain.AfterSaleStatus>anyList()))
                .thenReturn(Collections.emptyList());
        Mockito.when(sales.findBySubOrderNoOrderByIdAsc("SUB-1")).thenReturn(Collections.<AfterSale>emptyList());
        AfterSaleService service = new AfterSaleService(sales, Mockito.mock(AfterSaleItemRepository.class),
                subOrders, orders, items, Mockito.mock(OrderEventRepository.class));

        try {
            service.create(new TradeActor(1L, ActorType.BUYER), duplicateItemCommand());
            Assertions.fail("duplicate order item must be rejected");
        } catch (TradeException expected) {
            // expected: one order item can be requested at most once per after-sale application
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void alreadyRefundedQuantityCannotBeRequestedAgain() {
        TradeSubOrder subOrder = shippedSubOrder();
        TradeOrder order = new TradeOrder("ORD-1", 1L, new BigDecimal("10.00"), BigDecimal.ZERO, "history", "key-1");
        OrderItem item = orderItem();
        AfterSale previous = new AfterSale("AS-OLD", "ORD-1", "SUB-1", 1L, 2L,
                AfterSaleType.REFUND_ONLY, new BigDecimal("10.00"), "missing");
        previous.approve("approved");
        previous.refund();
        AfterSaleItem previousItem = new AfterSaleItem("AS-OLD", item, 1, new BigDecimal("10.00"));
        TradeSubOrderRepository subOrders = Mockito.mock(TradeSubOrderRepository.class);
        TradeOrderRepository orders = Mockito.mock(TradeOrderRepository.class);
        OrderItemRepository items = Mockito.mock(OrderItemRepository.class);
        AfterSaleRepository sales = Mockito.mock(AfterSaleRepository.class);
        AfterSaleItemRepository saleItems = Mockito.mock(AfterSaleItemRepository.class);
        Mockito.when(subOrders.findBySubOrderNoForUpdate("SUB-1")).thenReturn(subOrder);
        Mockito.when(orders.findByOrderNo("ORD-1")).thenReturn(order);
        Mockito.when(items.findBySubOrderNo("SUB-1")).thenReturn(Collections.singletonList(item));
        Mockito.when(sales.findBySubOrderNoAndStatusIn(Mockito.eq("SUB-1"), Mockito.<com.own.order.domain.AfterSaleStatus>anyList()))
                .thenReturn(Collections.emptyList());
        Mockito.when(sales.findBySubOrderNoOrderByIdAsc("SUB-1")).thenReturn(Collections.singletonList(previous));
        Mockito.when(saleItems.findByAfterSaleNo("AS-OLD")).thenReturn(Collections.singletonList(previousItem));
        AfterSaleService service = new AfterSaleService(sales, saleItems, subOrders, orders, items,
                Mockito.mock(OrderEventRepository.class));

        try {
            service.create(new TradeActor(1L, ActorType.BUYER), singleItemCommand());
            Assertions.fail("previously refunded quantity must not be requested again");
        } catch (TradeException expected) {
            // expected
        }
    }

    @Test
    public void afterSaleDetailIsVisibleOnlyToItsBuyerOrMerchant() {
        AfterSale sale = new AfterSale("AS-1", "ORD-1", "SUB-1", 1L, 2L,
                AfterSaleType.REFUND_ONLY, BigDecimal.ONE, "missing");
        AfterSaleRepository sales = Mockito.mock(AfterSaleRepository.class);
        AfterSaleItemRepository saleItems = Mockito.mock(AfterSaleItemRepository.class);
        Mockito.when(sales.findByAfterSaleNo("AS-1")).thenReturn(sale);
        Mockito.when(saleItems.findByAfterSaleNo("AS-1")).thenReturn(Collections.<AfterSaleItem>emptyList());
        OrderEventRepository events = Mockito.mock(OrderEventRepository.class);
        Mockito.when(events.findByAfterSaleNoOrderByIdAsc("AS-1")).thenReturn(Collections.emptyList());
        AfterSaleService service = new AfterSaleService(sales, saleItems,
                Mockito.mock(TradeSubOrderRepository.class), Mockito.mock(TradeOrderRepository.class),
                Mockito.mock(OrderItemRepository.class), events);

        AfterSaleDetail detail = service.detail(new TradeActor(2L, ActorType.MERCHANT), "AS-1");
        Assertions.assertEquals("AS-1", detail.getAfterSale().getAfterSaleNo());
        Assertions.assertNotNull(detail.getEvents());
        try {
            service.detail(new TradeActor(3L, ActorType.MERCHANT), "AS-1");
            Assertions.fail("other merchants must not read an after-sale request");
        } catch (TradeException expected) {
            // expected
        }
    }

    @Test
    public void duplicateEvidenceIsRejectedBeforeAnyRemoteFileValidation() {
        TradeSubOrderRepository subOrders = Mockito.mock(TradeSubOrderRepository.class);
        TradeOrderRepository orders = Mockito.mock(TradeOrderRepository.class);
        OrderItemRepository orderItems = Mockito.mock(OrderItemRepository.class);
        AfterSaleRepository sales = Mockito.mock(AfterSaleRepository.class);
        Mockito.when(subOrders.findBySubOrderNoForUpdate("SUB-1")).thenReturn(shippedSubOrder());
        Mockito.when(orders.findByOrderNo("ORD-1")).thenReturn(new TradeOrder("ORD-1", 1L, BigDecimal.TEN,
                BigDecimal.ZERO, "history", "key-1"));
        Mockito.when(orderItems.findBySubOrderNo("SUB-1")).thenReturn(Collections.singletonList(orderItem()));
        Mockito.when(sales.findBySubOrderNoAndStatusIn(Mockito.eq("SUB-1"), Mockito.<com.own.order.domain.AfterSaleStatus>anyList()))
                .thenReturn(Collections.<AfterSale>emptyList());
        Mockito.when(sales.findBySubOrderNoOrderByIdAsc("SUB-1")).thenReturn(Collections.<AfterSale>emptyList());
        AfterSaleService service = new AfterSaleService(sales, Mockito.mock(AfterSaleItemRepository.class), subOrders,
                orders, orderItems, Mockito.mock(OrderEventRepository.class));
        FileClient files = Mockito.mock(FileClient.class);
        ReflectionTestUtils.setField(service, "files", files);
        CreateAfterSaleCommand command = singleItemCommand();
        command.setEvidenceFileNames(Arrays.asList("proof.jpg", "proof.jpg"));

        try {
            service.create(new TradeActor(1L, ActorType.BUYER), command);
            Assertions.fail("duplicate evidence must be rejected before processing the order");
        } catch (TradeException expected) {
            Assertions.assertEquals(422, expected.getStatus());
        }
        Mockito.verifyNoInteractions(files);
    }

    @Test
    public void simulatedRefundCallbackCompletesRefundOnlyWithoutRestocking() {
        AfterSale sale = new AfterSale("AS-1", "ORD-1", "SUB-1", 1L, 2L,
                AfterSaleType.REFUND_ONLY, new BigDecimal("6.00"), "missing");
        sale.approve("approved");
        AfterSaleRepository sales = Mockito.mock(AfterSaleRepository.class);
        Mockito.when(sales.findByAfterSaleNoForUpdate("AS-1")).thenReturn(sale);
        AfterSaleService service = new AfterSaleService(sales, Mockito.mock(AfterSaleItemRepository.class),
                Mockito.mock(TradeSubOrderRepository.class), Mockito.mock(TradeOrderRepository.class),
                Mockito.mock(OrderItemRepository.class), Mockito.mock(OrderEventRepository.class));
        InventoryClient inventory = Mockito.mock(InventoryClient.class);
        ReflectionTestUtils.setField(service, "inventory", inventory);
        AfterSale completed = service.refundSucceeded(new TradeActor(1L, ActorType.SYSTEM), "AS-1");
        Assertions.assertEquals(com.own.order.domain.AfterSaleStatus.REFUNDED, completed.getStatus());
        Mockito.verifyNoInteractions(inventory);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void returnAndRefundRestocksExactlyOnceAfterRefundCallback() {
        AfterSale sale = new AfterSale("AS-2", "ORD-2", "SUB-2", 1L, 2L,
                AfterSaleType.RETURN_AND_REFUND, new BigDecimal("6.00"), "damaged");
        sale.approve("approved");
        sale.submitReturn("DEMO", "RETURN-1");
        sale.receiveReturn();
        AfterSaleItemRepository saleItems = Mockito.mock(AfterSaleItemRepository.class);
        AfterSaleRepository sales = Mockito.mock(AfterSaleRepository.class);
        Mockito.when(sales.findByAfterSaleNoForUpdate("AS-2")).thenReturn(sale);
        Mockito.when(saleItems.findByAfterSaleNo("AS-2"))
                .thenReturn(Collections.singletonList(Mockito.mock(AfterSaleItem.class)));
        AfterSaleService service = new AfterSaleService(sales, saleItems,
                Mockito.mock(TradeSubOrderRepository.class), Mockito.mock(TradeOrderRepository.class),
                Mockito.mock(OrderItemRepository.class), Mockito.mock(OrderEventRepository.class));
        InventoryClient inventory = Mockito.mock(InventoryClient.class);
        ReflectionTestUtils.setField(service, "inventory", inventory);

        service.refundSucceeded(new TradeActor(1L, ActorType.SYSTEM), "AS-2");
        service.refundSucceeded(new TradeActor(1L, ActorType.SYSTEM), "AS-2");

        Assertions.assertEquals(com.own.order.domain.AfterSaleStatus.REFUNDED, sale.getStatus());
        Mockito.verify(inventory, Mockito.times(1)).refundAfterSale(Mockito.eq("AS-2"), Mockito.eq(2L),
                Mockito.<AfterSaleItem>anyList());
    }

    @Test
    public void afterSaleStateChangesUseLockedRead() {
        AfterSaleRepository sales = Mockito.mock(AfterSaleRepository.class);
        AfterSaleService service = new AfterSaleService(sales, Mockito.mock(AfterSaleItemRepository.class),
                Mockito.mock(TradeSubOrderRepository.class), Mockito.mock(TradeOrderRepository.class),
                Mockito.mock(OrderItemRepository.class), Mockito.mock(OrderEventRepository.class));

        AfterSale rejected = new AfterSale("AS-AUDIT", "ORD-1", "SUB-1", 1L, 2L,
                AfterSaleType.REFUND_ONLY, BigDecimal.ONE, "missing");
        Mockito.when(sales.findByAfterSaleNoForUpdate("AS-AUDIT")).thenReturn(rejected);
        AfterSaleAuditCommand audit = new AfterSaleAuditCommand();
        audit.setApproved(false); audit.setRemark("rejected");
        service.audit(new TradeActor(2L, ActorType.MERCHANT), "AS-AUDIT", audit);

        AfterSale returning = new AfterSale("AS-RETURN", "ORD-1", "SUB-1", 1L, 2L,
                AfterSaleType.RETURN_AND_REFUND, BigDecimal.ONE, "missing");
        returning.approve("approved");
        Mockito.when(sales.findByAfterSaleNoForUpdate("AS-RETURN")).thenReturn(returning);
        ReturnShipmentCommand shipment = new ReturnShipmentCommand();
        shipment.setLogisticsCompany("DEMO"); shipment.setTrackingNo("TRACK-1");
        service.submitReturn(new TradeActor(1L, ActorType.BUYER), "AS-RETURN", shipment);

        AfterSale received = new AfterSale("AS-RECEIVE", "ORD-1", "SUB-1", 1L, 2L,
                AfterSaleType.RETURN_AND_REFUND, BigDecimal.ONE, "missing");
        received.approve("approved"); received.submitReturn("DEMO", "TRACK-2");
        Mockito.when(sales.findByAfterSaleNoForUpdate("AS-RECEIVE")).thenReturn(received);
        SettlementClient settlement = Mockito.mock(SettlementClient.class);
        ReflectionTestUtils.setField(service, "settlement", settlement);
        service.receiveReturn(new TradeActor(2L, ActorType.MERCHANT), "AS-RECEIVE");

        Mockito.verify(sales).findByAfterSaleNoForUpdate("AS-AUDIT");
        Mockito.verify(sales).findByAfterSaleNoForUpdate("AS-RETURN");
        Mockito.verify(sales).findByAfterSaleNoForUpdate("AS-RECEIVE");
        Mockito.verify(settlement).refundAfterSale(received);
    }

    @Test
    public void buyerCanCloseOnlyOwnApplyingAfterSaleWithoutRemoteSideEffects() {
        AfterSale sale = new AfterSale("AS-CLOSE", "ORD-1", "SUB-1", 1L, 2L,
                AfterSaleType.REFUND_ONLY, BigDecimal.ONE, "no longer needed");
        AfterSaleRepository sales = Mockito.mock(AfterSaleRepository.class);
        OrderEventRepository events = Mockito.mock(OrderEventRepository.class);
        Mockito.when(sales.findByAfterSaleNoForUpdate("AS-CLOSE")).thenReturn(sale);
        AfterSaleService service = new AfterSaleService(sales, Mockito.mock(AfterSaleItemRepository.class),
                Mockito.mock(TradeSubOrderRepository.class), Mockito.mock(TradeOrderRepository.class),
                Mockito.mock(OrderItemRepository.class), events);

        AfterSale closed = service.close(new TradeActor(1L, ActorType.BUYER), "AS-CLOSE");

        Assertions.assertEquals(com.own.order.domain.AfterSaleStatus.CLOSED, closed.getStatus());
        Mockito.verify(sales).save(sale);
        Mockito.verify(events).save(Mockito.any(com.own.order.domain.OrderEvent.class));
        try {
            service.close(new TradeActor(2L, ActorType.BUYER), "AS-CLOSE");
            Assertions.fail("another buyer must not close the request");
        } catch (TradeException expected) {
            Assertions.assertEquals(403, expected.getStatus());
        }
    }

    @Test
    public void onlyTheBuyerCanConfirmReplacementReceiptAfterMerchantShipment() {
        AfterSale sale = new AfterSale("AS-EXCHANGE", "ORD-1", "SUB-1", 1L, 2L,
                AfterSaleType.EXCHANGE, BigDecimal.ONE, "wrong size");
        sale.approve("approved");
        sale.submitReturn("DEMO", "RETURN-1");
        sale.receiveReturn();
        sale.exchangeShip("DEMO", "REPLACEMENT-1");
        AfterSaleRepository sales = Mockito.mock(AfterSaleRepository.class);
        Mockito.when(sales.findByAfterSaleNoForUpdate("AS-EXCHANGE")).thenReturn(sale);
        AfterSaleService service = new AfterSaleService(sales, Mockito.mock(AfterSaleItemRepository.class),
                Mockito.mock(TradeSubOrderRepository.class), Mockito.mock(TradeOrderRepository.class),
                Mockito.mock(OrderItemRepository.class), Mockito.mock(OrderEventRepository.class));

        Assertions.assertEquals(com.own.order.domain.AfterSaleStatus.EXCHANGED,
                service.confirmExchangeReceipt(new TradeActor(1L, ActorType.BUYER), "AS-EXCHANGE").getStatus());
        try {
            service.confirmExchangeReceipt(new TradeActor(2L, ActorType.BUYER), "AS-EXCHANGE");
            Assertions.fail("another buyer must not confirm replacement receipt");
        } catch (TradeException expected) {
            Assertions.assertEquals(403, expected.getStatus());
        }
    }

    private CreateAfterSaleCommand duplicateItemCommand() {
        AfterSaleItemCommand first = new AfterSaleItemCommand();
        first.setOrderItemId(11L);
        first.setQuantity(1);
        AfterSaleItemCommand duplicate = new AfterSaleItemCommand();
        duplicate.setOrderItemId(11L);
        duplicate.setQuantity(1);
        CreateAfterSaleCommand command = new CreateAfterSaleCommand();
        command.setSubOrderNo("SUB-1");
        command.setType(AfterSaleType.REFUND_ONLY);
        command.setReason("missing");
        command.setItems(Arrays.asList(first, duplicate));
        return command;
    }

    private CreateAfterSaleCommand singleItemCommand() {
        AfterSaleItemCommand line = new AfterSaleItemCommand();
        line.setOrderItemId(11L);
        line.setQuantity(1);
        CreateAfterSaleCommand command = new CreateAfterSaleCommand();
        command.setSubOrderNo("SUB-1");
        command.setType(AfterSaleType.REFUND_ONLY);
        command.setReason("missing");
        command.setItems(Collections.singletonList(line));
        return command;
    }

    private TradeSubOrder shippedSubOrder() {
        TradeSubOrder subOrder = new TradeSubOrder("SUB-1", "ORD-1", 2L,
                new BigDecimal("10.00"), BigDecimal.ZERO, BigDecimal.ZERO);
        subOrder.markToShip();
        subOrder.ship("DEMO", "TRACK-1");
        return subOrder;
    }

    private OrderItem orderItem() {
        OrderItem item = new OrderItem("ORD-1", "SUB-1",
                new CartItem(1L, 101L, 2L, "product", new BigDecimal("10.00"), 1));
        ReflectionTestUtils.setField(item, "id", 11L);
        return item;
    }
}
