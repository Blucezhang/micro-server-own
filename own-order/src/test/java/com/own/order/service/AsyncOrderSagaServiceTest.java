package com.own.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.order.domain.CartItem;
import com.own.order.domain.OrderEvent;
import com.own.order.domain.OrderSaga;
import com.own.order.domain.OrderSagaStatus;
import com.own.order.domain.OrderStatus;
import com.own.order.domain.SubOrderStatus;
import com.own.order.domain.TradeOrder;
import com.own.order.domain.TradeSubOrder;
import com.own.order.dto.OrderSagaCommand;
import com.own.order.dto.CouponUse;
import com.own.order.repository.CartItemRepository;
import com.own.order.repository.OrderEventRepository;
import com.own.order.repository.OrderSagaRepository;
import com.own.order.repository.TradeOrderRepository;
import com.own.order.repository.TradeSubOrderRepository;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class AsyncOrderSagaServiceTest {
    @Test
    void reservesThenMakesTheOrderPayableAndDeletesOnlySelectedCartRows() throws Exception {
        Fixture fixture = new Fixture(false);

        fixture.service.process("ORD-1");
        assertEquals(1, fixture.inventory.reserveCalls);
        assertEquals(OrderSagaStatus.PROCESSING, fixture.saga.getStatus());
        fixture.service.process("ORD-1");

        assertEquals(1, fixture.coupons.reserveCalls);
        assertEquals(OrderSagaStatus.COMPLETED, fixture.saga.getStatus());
        assertEquals(OrderStatus.PENDING_PAYMENT, fixture.order.getStatus());
        assertEquals(SubOrderStatus.PENDING_PAYMENT, fixture.subOrder.getStatus());
        assertEquals(Collections.singletonList(9L), fixture.deletedCartIds);
    }

    @Test
    void failedReservationKeepsTheCartAndReleasesAnyPartialResourcesBeforeFailure() throws Exception {
        Fixture fixture = new Fixture(true);

        fixture.service.process("ORD-1");
        assertEquals(OrderSagaStatus.COMPENSATING, fixture.saga.getStatus());
        fixture.service.process("ORD-1");

        assertEquals(OrderSagaStatus.FAILED, fixture.saga.getStatus());
        assertEquals(OrderStatus.SAGA_FAILED, fixture.order.getStatus());
        assertEquals(SubOrderStatus.CANCELED, fixture.subOrder.getStatus());
        assertEquals(1, fixture.inventory.releaseCalls);
        assertEquals(1, fixture.coupons.releaseCalls);
        assertTrue(fixture.deletedCartIds.isEmpty());
    }

    private static final class Fixture {
        private final TradeOrder order = new TradeOrder("ORD-1", 7L, BigDecimal.TEN, BigDecimal.ZERO, "address", "key");
        private final TradeSubOrder subOrder = new TradeSubOrder("SUB-1", "ORD-1", 8L, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO);
        private final List<Long> deletedCartIds = new ArrayList<Long>();
        private final RecordingInventory inventory;
        private final RecordingCoupons coupons = new RecordingCoupons();
        private final OrderSaga saga;
        private final AsyncOrderSagaService service;

        private Fixture(boolean failInventory) throws Exception {
            ObjectMapper mapper = new ObjectMapper();
            CouponUse coupon = new CouponUse(); coupon.setCouponNo("CPN-1"); coupon.setMerchantId(8L); coupon.setApplicableAmount(BigDecimal.TEN);
            OrderSagaCommand command = new OrderSagaCommand("SAGA-1", "ORD-1", 7L, BigDecimal.TEN,
                    Collections.singletonList(new OrderSagaCommand.InventoryLine(101L, 8L, 2)), Collections.singletonList(coupon));
            command.setCartItemIds(Collections.singletonList(9L));
            saga = new OrderSaga("SAGA-1", "ORD-1", mapper.writeValueAsString(command));
            inventory = new RecordingInventory(failInventory);
            service = new AsyncOrderSagaService(sagas(), orders(), subOrders(), carts(), events(), inventory, coupons, mapper, 1, 15);
            order.startSaga();
            subOrder.startSaga();
        }

        private OrderSagaRepository sagas() {
            return proxy(OrderSagaRepository.class, (name, args) -> {
                if ("findLockedByOrderNo".equals(name)) return saga;
                if ("save".equals(name)) return args[0];
                return defaultValue(name);
            });
        }

        private TradeOrderRepository orders() {
            return proxy(TradeOrderRepository.class, (name, args) -> {
                if ("findLockedByOrderNo".equals(name)) return order;
                if ("save".equals(name)) return args[0];
                return defaultValue(name);
            });
        }

        private TradeSubOrderRepository subOrders() {
            return proxy(TradeSubOrderRepository.class, (name, args) -> {
                if ("findByOrderNo".equals(name)) return Collections.singletonList(subOrder);
                if ("save".equals(name)) return args[0];
                return defaultValue(name);
            });
        }

        private CartItemRepository carts() {
            CartItem cart = new CartItem(7L, 101L, 8L, "sku", BigDecimal.TEN, 2);
            return proxy(CartItemRepository.class, (name, args) -> {
                if ("findByIdAndBuyerId".equals(name)) return cart;
                if ("delete".equals(name)) { deletedCartIds.add(9L); return null; }
                return defaultValue(name);
            });
        }

        private OrderEventRepository events() {
            return proxy(OrderEventRepository.class, (name, args) -> "save".equals(name) ? args[0] : defaultValue(name));
        }
    }

    private interface Invocation { Object call(String method, Object[] args); }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (target, method, args) -> invocation.call(method.getName(), args));
    }

    private static Object defaultValue(String method) {
        if ("toString".equals(method)) return "test-proxy";
        return null;
    }

    private static final class RecordingInventory extends InventoryClient {
        private final boolean failReserve;
        private int reserveCalls;
        private int releaseCalls;
        private RecordingInventory(boolean failReserve) { super(new RestTemplate(), ""); this.failReserve = failReserve; }
        @Override public void reserve(String orderNo, Long productId, Long merchantId, Integer quantity) {
            reserveCalls++;
            if (failReserve) throw new IllegalStateException("inventory unavailable");
        }
        @Override public void release(String orderNo) { releaseCalls++; }
    }

    private static final class RecordingCoupons extends CouponClient {
        private int reserveCalls;
        private int releaseCalls;
        private RecordingCoupons() { super(new RestTemplate(), ""); }
        @Override public CouponReservation reserve(String orderNo, Long buyerId, BigDecimal parentAmount,
                                                   Map<Long, BigDecimal> merchantAmounts, List<com.own.order.dto.CouponUse> uses) {
            reserveCalls++;
            return new CouponReservation(BigDecimal.ZERO, new HashMap<Long, BigDecimal>());
        }
        @Override public void release(String orderNo) { releaseCalls++; }
    }
}
