package com.own.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.order.domain.CartItem;
import com.own.order.domain.OrderEvent;
import com.own.order.domain.OrderSaga;
import com.own.order.domain.OrderSagaStatus;
import com.own.order.domain.OrderSagaStep;
import com.own.order.domain.OrderStatus;
import com.own.order.domain.TradeOrder;
import com.own.order.domain.TradeSubOrder;
import com.own.order.dto.OrderSagaCommand;
import com.own.order.repository.CartItemRepository;
import com.own.order.repository.OrderEventRepository;
import com.own.order.repository.OrderSagaRepository;
import com.own.order.repository.TradeOrderRepository;
import com.own.order.repository.TradeSubOrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists every checkout step before a downstream service is called.  A retry
 * never advances the order twice because inventory and coupon operations use
 * the order number as their idempotency boundary.
 */
@Service
public class AsyncOrderSagaService {
    private final OrderSagaRepository sagas;
    private final TradeOrderRepository orders;
    private final TradeSubOrderRepository subOrders;
    private final CartItemRepository carts;
    private final OrderEventRepository events;
    private final InventoryClient inventory;
    private final CouponClient coupons;
    private final ObjectMapper objectMapper;
    private final int retrySeconds;
    private final int reservationTtlMinutes;

    public AsyncOrderSagaService(OrderSagaRepository sagas, TradeOrderRepository orders,
                                 TradeSubOrderRepository subOrders, CartItemRepository carts,
                                 OrderEventRepository events, InventoryClient inventory,
                                 CouponClient coupons, ObjectMapper objectMapper,
                                 @Value("${trade.saga.retry-seconds:30}") int retrySeconds,
                                 @Value("${trade.saga.reservation-ttl-minutes:15}") int reservationTtlMinutes) {
        this.sagas = sagas;
        this.orders = orders;
        this.subOrders = subOrders;
        this.carts = carts;
        this.events = events;
        this.inventory = inventory;
        this.coupons = coupons;
        this.objectMapper = objectMapper;
        this.retrySeconds = retrySeconds;
        this.reservationTtlMinutes = reservationTtlMinutes;
    }

    @Transactional
    public OrderSaga start(TradeOrder order, List<TradeSubOrder> children, OrderSagaCommand command) {
        order.startSaga();
        orders.save(order);
        for (TradeSubOrder child : children) {
            child.startSaga();
            subOrders.save(child);
        }
        String sagaNo = "SAGA-" + UUID.randomUUID().toString();
        command.setSagaNo(sagaNo);
        OrderSaga saga = new OrderSaga(sagaNo, order.getOrderNo(), encode(command));
        sagas.save(saga);
        event(order.getOrderNo(), "SAGA_STARTED", "reserve_inventory");
        return saga;
    }

    @Transactional
    public void process(String orderNo) {
        OrderSaga saga = sagas.findLockedByOrderNo(orderNo);
        if (saga == null || saga.getStatus() == OrderSagaStatus.COMPLETED || saga.getStatus() == OrderSagaStatus.FAILED) return;
        try {
            if (saga.getStatus() == OrderSagaStatus.PROCESSING && expired(saga)) {
                saga.compensate("saga reservation expired", OrderSagaStep.COMPENSATE_INVENTORY);
                sagas.save(saga);
                event(orderNo, "SAGA_COMPENSATION_STARTED", "saga reservation expired");
                return;
            }
            if (saga.getStatus() == OrderSagaStatus.COMPENSATING) {
                compensate(saga);
                return;
            }
            OrderSagaCommand command = decode(saga);
            if (saga.getCurrentStep() == OrderSagaStep.RESERVE_INVENTORY) {
                reserveInventory(command);
                saga.advance(OrderSagaStep.RESERVE_COUPON);
                sagas.save(saga);
                event(orderNo, "SAGA_INVENTORY_RESERVED", "reserve_coupon");
                return;
            }
            if (saga.getCurrentStep() == OrderSagaStep.RESERVE_COUPON) {
                reserveCoupons(command);
                readyForPayment(saga, command);
                return;
            }
            throw new IllegalStateException("unsupported saga step " + saga.getCurrentStep());
        } catch (RuntimeException exception) {
            if (saga.getStatus() == OrderSagaStatus.PROCESSING) {
                saga.compensate(message(exception), OrderSagaStep.COMPENSATE_INVENTORY);
                event(orderNo, "SAGA_COMPENSATION_STARTED", message(exception));
            } else {
                saga.retry(message(exception), retrySeconds);
                event(orderNo, "SAGA_COMPENSATION_RETRY", message(exception));
            }
            sagas.save(saga);
        }
    }

    private void reserveInventory(OrderSagaCommand command) {
        for (OrderSagaCommand.InventoryLine line : command.getInventoryLines()) {
            inventory.reserve(command.getOrderNo(), line.getProductId(), line.getMerchantId(), line.getQuantity());
        }
    }

    private void reserveCoupons(OrderSagaCommand command) {
        if (command.getCoupons().isEmpty()) return;
        Map<Long, BigDecimal> amounts = new HashMap<Long, BigDecimal>();
        for (TradeSubOrder subOrder : subOrders.findByOrderNo(command.getOrderNo())) {
            amounts.put(subOrder.getMerchantId(), subOrder.getTotalAmount());
        }
        coupons.reserve(command.getOrderNo(), command.getBuyerId(), command.getTotalAmount(), amounts, command.getCoupons());
    }

    private void readyForPayment(OrderSaga saga, OrderSagaCommand command) {
        TradeOrder order = requiredOrder(command.getOrderNo());
        if (order.getStatus() != OrderStatus.PROCESSING) throw new IllegalStateException("order is no longer processing");
        order.sagaReadyForPayment();
        orders.save(order);
        for (TradeSubOrder child : subOrders.findByOrderNo(command.getOrderNo())) {
            child.sagaReadyForPayment();
            subOrders.save(child);
        }
        for (Long cartId : command.getCartItemIds()) {
            CartItem cart = carts.findByIdAndBuyerId(cartId, command.getBuyerId());
            if (cart != null) carts.delete(cart);
        }
        saga.complete();
        sagas.save(saga);
        event(command.getOrderNo(), "SAGA_COMPLETED", "pending_payment");
    }

    private void compensate(OrderSaga saga) {
        OrderSagaCommand command = decode(saga);
        coupons.release(command.getOrderNo());
        inventory.release(command.getOrderNo());
        TradeOrder order = requiredOrder(command.getOrderNo());
        if (order.getStatus() == OrderStatus.PROCESSING) {
            order.sagaFailed();
            orders.save(order);
            for (TradeSubOrder child : subOrders.findByOrderNo(command.getOrderNo())) {
                child.sagaFailed();
                subOrders.save(child);
            }
        }
        saga.fail();
        sagas.save(saga);
        event(command.getOrderNo(), "SAGA_FAILED", saga.getLastError());
    }

    private TradeOrder requiredOrder(String orderNo) {
        TradeOrder order = orders.findLockedByOrderNo(orderNo);
        if (order == null) throw new IllegalStateException("saga order is missing");
        return order;
    }

    private String encode(OrderSagaCommand command) {
        try { return objectMapper.writeValueAsString(command); }
        catch (JsonProcessingException exception) { throw new IllegalStateException("cannot persist saga command", exception); }
    }

    private OrderSagaCommand decode(OrderSaga saga) {
        try { return objectMapper.readValue(saga.getCommandPayload(), OrderSagaCommand.class); }
        catch (Exception exception) { throw new IllegalStateException("cannot read saga command", exception); }
    }

    private void event(String orderNo, String type, String payload) {
        events.save(new OrderEvent(orderNo, null, type, 0L, ActorType.SYSTEM.name(), payload == null ? "" : payload));
    }

    private String message(Exception exception) {
        String value = exception.getMessage();
        String result = value == null || value.trim().isEmpty() ? exception.getClass().getSimpleName() : value;
        return result.substring(0, Math.min(result.length(), 500));
    }

    private boolean expired(OrderSaga saga) {
        long ttlMillis = Math.max(1, reservationTtlMinutes) * 60L * 1000L;
        return saga.getCreatedAt().getTime() < System.currentTimeMillis() - ttlMillis;
    }
}
