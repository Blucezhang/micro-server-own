package com.own.order.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.order.domain.CartItem;
import com.own.order.domain.AfterSaleStatus;
import com.own.order.domain.OrderEvent;
import com.own.order.domain.OrderItem;
import com.own.order.domain.OrderStatus;
import com.own.order.domain.SubOrderStatus;
import com.own.order.domain.TradeOrder;
import com.own.order.domain.TradeSubOrder;
import com.own.order.dto.AddCartItemCommand;
import com.own.order.dto.CheckoutCommand;
import com.own.order.dto.CheckoutQuote;
import com.own.order.dto.CouponUse;
import com.own.order.dto.OrderDetail;
import com.own.order.dto.ShipmentCommand;
import com.own.order.dto.UpdateCartItemCommand;
import com.own.order.repository.CartItemRepository;
import com.own.order.repository.OrderEventRepository;
import com.own.order.repository.OrderItemRepository;
import com.own.order.repository.SensitiveAccessAuditRepository;
import com.own.order.repository.TradeOrderRepository;
import com.own.order.repository.TradeSubOrderRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Date;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Service
public class OrderService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderService.class);

    private final CartItemRepository cartItemRepository;
    private final com.own.order.repository.CartBuyerGuardRepository cartBuyerGuards;
    private final TradeOrderRepository orderRepository;
    private final TradeSubOrderRepository subOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventRepository eventRepository;
    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;
    private final CouponClient couponClient;
    private final AddressClient addressClient;
    private final com.own.order.repository.MerchantFreightRuleRepository freightRuleRepository;
    private final com.own.order.repository.LogisticsTraceRepository logisticsTraceRepository;
    private final com.own.order.repository.AfterSaleRepository afterSaleRepository;
    private final SensitiveAccessAuditRepository sensitiveAccessAuditRepository;

    public OrderService(CartItemRepository cartItemRepository, com.own.order.repository.CartBuyerGuardRepository cartBuyerGuards, TradeOrderRepository orderRepository,
                        TradeSubOrderRepository subOrderRepository, OrderItemRepository orderItemRepository,
                        OrderEventRepository eventRepository, CatalogClient catalogClient,
                        InventoryClient inventoryClient, CouponClient couponClient, AddressClient addressClient,
                        com.own.order.repository.MerchantFreightRuleRepository freightRuleRepository,
                        com.own.order.repository.LogisticsTraceRepository logisticsTraceRepository,
                        com.own.order.repository.AfterSaleRepository afterSaleRepository,
                        SensitiveAccessAuditRepository sensitiveAccessAuditRepository) {
        this.cartItemRepository = cartItemRepository;
        this.cartBuyerGuards = cartBuyerGuards;
        this.orderRepository = orderRepository;
        this.subOrderRepository = subOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.eventRepository = eventRepository;
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
        this.couponClient = couponClient;
        this.addressClient = addressClient;
        this.freightRuleRepository = freightRuleRepository;
        this.logisticsTraceRepository = logisticsTraceRepository;
        this.afterSaleRepository = afterSaleRepository;
        this.sensitiveAccessAuditRepository = sensitiveAccessAuditRepository;
    }

    @Transactional
    public CartItem addCartItem(TradeActor actor, AddCartItemCommand command) {
        actor.require(ActorType.BUYER);
        if (command == null || command.getProductId() == null || command.getQuantity() == null
                || command.getQuantity().intValue() <= 0) {
            throw TradeException.unprocessable("productId and positive quantity are required");
        }
        lockCart(actor.getId());
        CatalogClient.CatalogProduct product = catalogClient.getProduct(command.getProductId());
        CartItem existing = cartItemRepository.findByBuyerIdAndProductId(actor.getId(), product.getProductId());
        if (existing != null) {
            validateCatalogSnapshots(java.util.Collections.singletonList(existing));
            long total = existing.getQuantity().longValue() + command.getQuantity().longValue();
            if (total > Integer.MAX_VALUE) throw TradeException.unprocessable("cart item quantity is too large");
            existing.setQuantity(Integer.valueOf((int) total));
            return cartItemRepository.save(existing);
        }
        CartItem item = new CartItem(actor.getId(), product.getProductId(), product.getMerchantId(), product.getName(),
                product.getPrice(), command.getQuantity());
        return cartItemRepository.save(item);
    }

    public List<CartItem> cart(TradeActor actor) {
        actor.require(ActorType.BUYER);
        return cartItemRepository.findByBuyerIdOrderByIdAsc(actor.getId());
    }

    @Transactional
    public void removeCartItem(TradeActor actor, Long cartItemId) {
        actor.require(ActorType.BUYER);
        lockCart(actor.getId());
        CartItem item = cartItemRepository.findByIdAndBuyerId(cartItemId, actor.getId());
        if (item == null) {
            throw TradeException.notFound("cart item was not found");
        }
        cartItemRepository.delete(item);
    }

    @Transactional
    public CartItem updateCartItem(TradeActor actor, Long cartItemId, UpdateCartItemCommand command) {
        actor.require(ActorType.BUYER);
        if (command == null || command.getQuantity() == null || command.getQuantity().intValue() <= 0) {
            throw TradeException.unprocessable("positive quantity is required");
        }
        lockCart(actor.getId());
        CartItem item = cartItemRepository.findByIdAndBuyerId(cartItemId, actor.getId());
        if (item == null) throw TradeException.notFound("cart item was not found");
        // Updating a cart must not make an off-shelf or changed SKU look purchasable.
        validateCatalogSnapshots(java.util.Collections.singletonList(item));
        item.setQuantity(command.getQuantity());
        return cartItemRepository.save(item);
    }

    public CheckoutQuote quote(TradeActor actor, CheckoutCommand command) {
        actor.require(ActorType.BUYER);
        requireAddress(command, actor.getId());
        List<CartItem> items = selectedCart(actor.getId(), command);
        validateCatalogSnapshots(items);
        return quote(actor.getId(), items, command == null ? null : command.getCoupons(), null);
    }

    @Transactional
    public TradeOrder createOrder(TradeActor actor, CheckoutCommand command, String requestKey) {
        actor.require(ActorType.BUYER);
        TradeOrder existing = orderRepository.findByRequestKey(requestKey);
        if (existing != null) {
            if (!existing.getBuyerId().equals(actor.getId())) {
                throw TradeException.conflict("idempotency key belongs to another buyer");
            }
            return existing;
        }
        AddressClient.AddressSnapshot address = requireAddress(command, actor.getId());
        List<CartItem> items = selectedCart(actor.getId(), command);
        validateCatalogSnapshots(items);
        String orderNo = "ORD-" + UUID.randomUUID().toString();
        CheckoutQuote quote = quote(actor.getId(), items, command.getCoupons(), orderNo);
        TradeOrder order = new TradeOrder(orderNo, actor.getId(), quote.getTotalAmount(), quote.getDiscountAmount(),
                address.formatted(), address.id, address.recipientName, address.mobile, address.province, address.city,
                address.district, address.detail, freightTotal(quote.getMerchantFreights()), requestKey);
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        List<TradeSubOrder> subOrders = createSubOrders(orderNo, items, quote.getMerchantTotals(), quote.getMerchantDiscounts(), quote.getMerchantFreights());
        try {
            for (TradeSubOrder subOrder : subOrders) {
                subOrderRepository.save(subOrder);
            }
            for (CartItem item : items) {
                OrderItem orderItem = new OrderItem(orderNo, findSubOrderNo(subOrders, item.getMerchantId()), item);
                orderItemRepository.save(orderItem);
                orderItems.add(orderItem);
            }
            reserveInventory(orderNo, orderItems);
            orderRepository.save(order);
            event(orderNo, null, "ORDER_CREATED", actor, "pending_payment");
            for (CartItem item : items) {
                cartItemRepository.delete(item);
            }
            return order;
        } catch (RuntimeException exception) {
            inventoryClient.release(orderNo);
            couponClient.release(orderNo);
            throw exception;
        }
    }

    @Transactional
    public TradeOrder paymentSucceeded(String orderNo, TradeActor actor) {
        actor.require(ActorType.SYSTEM);
        TradeOrder order = requireLockedOrder(orderNo);
        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.FULFILLING
                || order.getStatus() == OrderStatus.COMPLETED) {
            return order;
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw TradeException.conflict("order cannot be paid from current status");
        }
        inventoryClient.commit(orderNo);
        couponClient.consume(orderNo);
        order.markPaid();
        orderRepository.save(order);
        for (TradeSubOrder subOrder : subOrderRepository.findByOrderNo(orderNo)) {
            subOrder.markToShip();
            subOrderRepository.save(subOrder);
        }
        event(orderNo, null, "PAYMENT_SUCCEEDED", actor, "to_ship");
        return order;
    }

    @Transactional
    public TradeOrder cancel(TradeActor actor, String orderNo) {
        actor.require(ActorType.BUYER);
        TradeOrder order = requireBuyerLockedOrder(orderNo, actor.getId());
        if (order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            order.cancel();
            for (TradeSubOrder subOrder : subOrderRepository.findByOrderNo(orderNo)) {
                subOrder.cancel();
                subOrderRepository.save(subOrder);
            }
            inventoryClient.release(orderNo);
            couponClient.release(orderNo);
            orderRepository.save(order);
            event(orderNo, null, "ORDER_CANCELED", actor, "unpaid");
            return order;
        }
        if (order.getStatus() == OrderStatus.PAID && allToShip(orderNo)) {
            order.startRefund();
            orderRepository.save(order);
            event(orderNo, null, "REFUND_REQUESTED", actor, "full_refund");
            return order;
        }
        throw TradeException.conflict("order cannot be canceled after shipment");
    }

    @Transactional
    public TradeOrder refundSucceeded(String orderNo, TradeActor actor) {
        actor.require(ActorType.SYSTEM);
        TradeOrder order = requireLockedOrder(orderNo);
        if (order.getStatus() == OrderStatus.REFUNDED) {
            return order;
        }
        if (order.getStatus() != OrderStatus.REFUNDING) {
            throw TradeException.conflict("order is not awaiting refund");
        }
        for (TradeSubOrder subOrder : subOrderRepository.findByOrderNo(orderNo)) {
            subOrder.refund();
            subOrderRepository.save(subOrder);
        }
        inventoryClient.refund(orderNo);
        couponClient.release(orderNo);
        order.refund();
        orderRepository.save(order);
        event(orderNo, null, "REFUND_SUCCEEDED", actor, "full_refund");
        return order;
    }

    @Transactional
    public TradeSubOrder ship(TradeActor actor, String subOrderNo, ShipmentCommand command) {
        actor.require(ActorType.MERCHANT);
        if (command == null || blank(command.getLogisticsCompany()) || blank(command.getTrackingNo())) {
            throw TradeException.unprocessable("logisticsCompany and trackingNo are required");
        }
        TradeSubOrder subOrder = subOrderRepository.findBySubOrderNo(subOrderNo);
        if (subOrder == null) {
            throw TradeException.notFound("sub order was not found");
        }
        if (!actor.getId().equals(subOrder.getMerchantId())) {
            throw TradeException.forbidden("merchant does not own this sub order");
        }
        // A buyer's paid cancellation changes the parent order to REFUNDING.
        // Lock and validate it before a merchant can turn a to-ship item into a
        // shipped one while the simulated payment refund is in flight.
        TradeOrder parent = requireLockedOrder(subOrder.getOrderNo());
        if (parent.getStatus() != OrderStatus.PAID) {
            throw TradeException.conflict("sub order cannot be shipped from current parent order status");
        }
        subOrder.ship(command.getLogisticsCompany().trim(), command.getTrackingNo().trim());
        subOrderRepository.save(subOrder);
        logisticsTraceRepository.save(new com.own.order.domain.LogisticsTrace(subOrderNo, "SHIPPED", "merchant shipped order", actor.getId()));
        parent.markFulfilling();
        orderRepository.save(parent);
        event(parent.getOrderNo(), subOrderNo, "SUB_ORDER_SHIPPED", actor, command.getTrackingNo());
        return subOrder;
    }

    @Transactional
    public TradeSubOrder receive(TradeActor actor, String subOrderNo) {
        actor.require(ActorType.BUYER);
        TradeSubOrder lookup = requireSubOrder(subOrderNo);
        TradeOrder parent = requireBuyerLockedOrder(lookup.getOrderNo(), actor.getId());
        // Parent order is the common transition lock. Re-load the sub-order with
        // a write lock after it so a buyer receipt and the scheduler cannot both
        // act on an earlier SHIPPED snapshot.
        TradeSubOrder subOrder = requireLockedSubOrder(subOrderNo);
        if (subOrder.getStatus() != SubOrderStatus.SHIPPED) {
            throw TradeException.conflict("sub order cannot be received from current status");
        }
        subOrder.receive();
        subOrderRepository.save(subOrder);
        if (allReceived(parent.getOrderNo())) {
            parent.markCompleted();
            orderRepository.save(parent);
        }
        event(parent.getOrderNo(), subOrderNo, "SUB_ORDER_RECEIVED", actor, "received");
        return subOrder;
    }

    @Transactional
    public TradeSubOrder correctShipment(TradeActor actor, String subOrderNo, ShipmentCommand command) {
        actor.require(ActorType.MERCHANT);
        if (command == null || blank(command.getLogisticsCompany()) || blank(command.getTrackingNo())) throw TradeException.unprocessable("logisticsCompany and trackingNo are required");
        TradeSubOrder lookup = requireSubOrder(subOrderNo);
        if (!actor.getId().equals(lookup.getMerchantId())) throw TradeException.forbidden("merchant does not own this sub order");
        TradeOrder parent = requireLockedOrder(lookup.getOrderNo());
        TradeSubOrder subOrder = requireLockedSubOrder(subOrderNo);
        if (parent.getStatus() != OrderStatus.FULFILLING || subOrder.getStatus() != SubOrderStatus.SHIPPED) throw TradeException.conflict("shipment can be corrected only before receipt");
        subOrder.correctShipment(command.getLogisticsCompany().trim(), command.getTrackingNo().trim());
        subOrderRepository.save(subOrder);
        logisticsTraceRepository.save(new com.own.order.domain.LogisticsTrace(subOrderNo, "SHIPMENT_CORRECTED", "merchant corrected shipment information", actor.getId()));
        event(parent.getOrderNo(), subOrderNo, "SUB_ORDER_SHIPMENT_CORRECTED", actor, "shipment_corrected");
        return subOrder;
    }

    @Transactional
    public com.own.order.domain.LogisticsTrace appendTrace(TradeActor actor, String subOrderNo, com.own.order.dto.LogisticsTraceCommand command) {
        actor.require(ActorType.MERCHANT); TradeSubOrder subOrder = subOrderRepository.findBySubOrderNo(subOrderNo);
        if (subOrder == null) throw TradeException.notFound("sub order was not found");
        if (!actor.getId().equals(subOrder.getMerchantId())) throw TradeException.forbidden("merchant does not own this sub order");
        if (subOrder.getStatus() != SubOrderStatus.SHIPPED) throw TradeException.conflict("logistics trace is available only after shipment");
        if (command == null || blank(command.getStatus()) || blank(command.getDetail())) throw TradeException.unprocessable("status and detail are required");
        return logisticsTraceRepository.save(new com.own.order.domain.LogisticsTrace(subOrderNo, command.getStatus().trim(), command.getDetail().trim(), actor.getId()));
    }

    public List<com.own.order.domain.LogisticsTrace> logisticsTraces(TradeActor actor, String subOrderNo) {
        TradeSubOrder subOrder = subOrderRepository.findBySubOrderNo(subOrderNo); if (subOrder == null) throw TradeException.notFound("sub order was not found");
        if (actor.getType() == ActorType.BUYER) requireBuyerOrder(subOrder.getOrderNo(), actor.getId());
        else if (actor.getType() == ActorType.MERCHANT && !actor.getId().equals(subOrder.getMerchantId())) throw TradeException.forbidden("merchant does not own this sub order");
        else if (actor.getType() != ActorType.BUYER && actor.getType() != ActorType.MERCHANT && actor.getType() != ActorType.SYSTEM) throw TradeException.forbidden("actor cannot read logistics");
        return logisticsTraceRepository.findBySubOrderNoOrderByIdAsc(subOrderNo);
    }

    @Transactional
    public boolean autoReceive(String subOrderNo) {
        TradeSubOrder lookup = subOrderRepository.findBySubOrderNo(subOrderNo);
        if (lookup == null || lookup.getStatus() != SubOrderStatus.SHIPPED) return false;
        TradeOrder parent = requireLockedOrder(lookup.getOrderNo());
        TradeSubOrder subOrder = requireLockedSubOrder(subOrderNo);
        if (subOrder.getStatus() != SubOrderStatus.SHIPPED) return false;
        if (!afterSaleRepository.findBySubOrderNoAndStatusIn(subOrderNo, Arrays.asList(AfterSaleStatus.APPLYING, AfterSaleStatus.APPROVED, AfterSaleStatus.RETURNING, AfterSaleStatus.RECEIVED, AfterSaleStatus.EXCHANGE_PENDING_SHIPMENT, AfterSaleStatus.EXCHANGE_SHIPPED, AfterSaleStatus.REFUND_PENDING)).isEmpty()) return false;
        subOrder.receive(); subOrderRepository.save(subOrder);
        logisticsTraceRepository.save(new com.own.order.domain.LogisticsTrace(subOrderNo, "AUTO_RECEIVED", "system auto receipt", 0L));
        if (allReceived(parent.getOrderNo())) { parent.markCompleted(); orderRepository.save(parent); }
        event(parent.getOrderNo(), subOrderNo, "SUB_ORDER_AUTO_RECEIVED", new TradeActor(0L, ActorType.SYSTEM), "auto_receipt");
        return true;
    }

    public TradeOrder findOrder(TradeActor actor, String orderNo) {
        actor.require(ActorType.BUYER);
        return requireBuyerOrder(orderNo, actor.getId());
    }

    public OrderDetail orderDetail(TradeActor actor, String orderNo) {
        actor.require(ActorType.BUYER);
        TradeOrder order = requireBuyerOrder(orderNo, actor.getId());
        auditSensitiveAddressAccess(actor, "BUYER_ORDER_ADDRESS_READ", orderNo);
        return new OrderDetail(order, subOrderRepository.findByOrderNo(orderNo),
                orderItemRepository.findByOrderNo(orderNo), timeline(eventRepository.findByOrderNoOrderByIdAsc(orderNo)));
    }

    public List<TradeOrder> findMyOrders(TradeActor actor) {
        actor.require(ActorType.BUYER);
        return orderRepository.findByBuyerIdOrderByIdDesc(actor.getId());
    }

    public Map<String, Object> buyerOrderPage(TradeActor actor, OrderStatus status, Date from, Date to, int page, int size) {
        actor.require(ActorType.BUYER);
        if (page < 0 || size < 1 || size > 100 || page > Integer.MAX_VALUE / size) throw TradeException.unprocessable("page must be nonnegative and size must be 1..100 without overflow");
        if (from != null && to != null && from.after(to)) throw TradeException.unprocessable("from must not be after to");
        Page<TradeOrder> result = orderRepository.pageByBuyer(actor.getId(), status, from, to, new PageRequest(page, size));
        Map<String, Object> response = new HashMap<String, Object>(); response.put("total", result.getTotalElements()); response.put("page", page); response.put("size", size); response.put("items", result.getContent()); return response;
    }

    /** Internal read model used to prevent reviews from accounts without a received item. */
    public boolean hasReceivedProduct(Long buyerId, Long productId) {
        if (buyerId == null || productId == null) return false;
        return orderItemRepository.countByBuyerProductAndSubOrderStatus(buyerId, productId, SubOrderStatus.RECEIVED) > 0L;
    }

    @Transactional
    public com.own.order.domain.MerchantFreightRule saveFreightRule(TradeActor actor, com.own.order.dto.FreightRuleCommand command) {
        actor.require(ActorType.MERCHANT);
        if (command == null || command.getFixedAmount() == null || command.getFixedAmount().compareTo(BigDecimal.ZERO) < 0
                || command.getFreeThreshold() != null && command.getFreeThreshold().compareTo(BigDecimal.ZERO) < 0) {
            throw TradeException.unprocessable("fixedAmount and freeThreshold must not be negative");
        }
        com.own.order.domain.MerchantFreightRule rule = freightRuleRepository.findByMerchantId(actor.getId());
        if (rule == null) rule = new com.own.order.domain.MerchantFreightRule(actor.getId(), command.getFixedAmount(), command.getFreeThreshold());
        else rule.update(command.getFixedAmount(), command.getFreeThreshold());
        return freightRuleRepository.save(rule);
    }

    public com.own.order.domain.MerchantFreightRule freightRule(TradeActor actor) { actor.require(ActorType.MERCHANT); return freightRuleRepository.findByMerchantId(actor.getId()); }

    public Map<String, Object> merchantSubOrders(TradeActor actor, SubOrderStatus status, Date from, Date to, int page, int size) {
        actor.require(ActorType.MERCHANT);
        if (page < 0 || size < 1 || size > 100 || page > Integer.MAX_VALUE / size) throw TradeException.unprocessable("page must be nonnegative and size must be 1..100 without overflow");
        List<TradeSubOrder> all = subOrderRepository.findByMerchantIdOrderByIdDesc(actor.getId()); List<TradeSubOrder> matched = new ArrayList<TradeSubOrder>();
        for (TradeSubOrder item : all) { TradeOrder order = orderRepository.findByOrderNo(item.getOrderNo()); if ((status == null || item.getStatus() == status) && (from == null || !order.getCreatedAt().before(from)) && (to == null || !order.getCreatedAt().after(to))) matched.add(item); }
        int start = Math.min(page * size, matched.size()), end = Math.min(start + size, matched.size()); Map<String,Object> result = new HashMap<String,Object>(); result.put("total", matched.size()); result.put("items", matched.subList(start,end)); return result;
    }

    public Map<String, Object> merchantSubOrderDetail(TradeActor actor, String subOrderNo) {
        actor.require(ActorType.MERCHANT); TradeSubOrder subOrder = subOrderRepository.findBySubOrderNo(subOrderNo);
        if (subOrder == null) throw TradeException.notFound("sub order was not found"); if (!actor.getId().equals(subOrder.getMerchantId())) throw TradeException.forbidden("merchant does not own this sub order");
        TradeOrder order = requireOrder(subOrder.getOrderNo()); Map<String,Object> result = new HashMap<String,Object>(); result.put("subOrder", subOrder); result.put("items", orderItemRepository.findBySubOrderNo(subOrderNo)); result.put("events", timeline(eventRepository.findBySubOrderNoOrderByIdAsc(subOrderNo)));
        Map<String,Object> address = new HashMap<String,Object>(); address.put("recipientName",order.getRecipientName());address.put("recipientMobile",order.getRecipientMobile());address.put("province",order.getRecipientProvince());address.put("city",order.getRecipientCity());address.put("district",order.getRecipientDistrict());address.put("detail",order.getRecipientDetail()); result.put("shippingAddress",address); auditSensitiveAddressAccess(actor,"MERCHANT_SUB_ORDER_ADDRESS_READ",order.getOrderNo()); return result;
    }

    public List<Map<String, Object>> settlementLines(TradeActor actor, String orderNo) {
        actor.require(ActorType.SYSTEM); requireOrder(orderNo);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (TradeSubOrder subOrder : subOrderRepository.findByOrderNo(orderNo)) {
            Map<String, Object> line = new LinkedHashMap<String, Object>();
            line.put("merchantId", subOrder.getMerchantId()); line.put("grossAmount", subOrder.getPayableAmount()); result.add(line);
        }
        return result;
    }

    public List<com.own.order.domain.SensitiveAccessAudit> sensitiveAccessAudits(TradeActor actor, String orderNo) {
        actor.require(ActorType.SYSTEM);
        return blank(orderNo) ? sensitiveAccessAuditRepository.findTop100ByOrderByIdDesc()
                : sensitiveAccessAuditRepository.findTop100ByResourceTypeAndResourceIdOrderByIdDesc("ORDER", orderNo);
    }

    public Map<String, Object> sensitiveAccessAuditPage(TradeActor actor, String orderNo, int page, int size) {
        actor.require(ActorType.SYSTEM);
        if (page < 0 || size < 1 || size > 100 || page > Integer.MAX_VALUE / size) throw TradeException.unprocessable("page must be nonnegative and size must be 1..100 without overflow");
        org.springframework.data.domain.Page<com.own.order.domain.SensitiveAccessAudit> result = blank(orderNo)
                ? sensitiveAccessAuditRepository.findAllByOrderByIdDesc(new org.springframework.data.domain.PageRequest(page, size))
                : sensitiveAccessAuditRepository.findByResourceTypeAndResourceIdOrderByIdDesc("ORDER", orderNo, new org.springframework.data.domain.PageRequest(page, size));
        Map<String, Object> data = new HashMap<String, Object>(); data.put("total", result.getTotalElements()); data.put("page", page); data.put("size", size); data.put("items", result.getContent()); return data;
    }

    @Transactional
    public boolean expirePendingOrder(String orderNo, Date deadline) {
        TradeOrder order = requireLockedOrder(orderNo);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT || !order.getCreatedAt().before(deadline)) return false;
        for (TradeSubOrder subOrder : subOrderRepository.findByOrderNo(orderNo)) {
            subOrder.cancel();
            subOrderRepository.save(subOrder);
        }
        inventoryClient.release(orderNo);
        couponClient.release(orderNo);
        order.cancel();
        orderRepository.save(order);
        event(orderNo, null, "ORDER_EXPIRED", new TradeActor(0L, ActorType.SYSTEM), "payment_timeout");
        return true;
    }

    private CheckoutQuote quote(Long buyerId, List<CartItem> items, List<CouponUse> coupons, String reservationOrderNo) {
        BigDecimal total = BigDecimal.ZERO;
        Map<Long, BigDecimal> merchantTotals = new HashMap<Long, BigDecimal>();
        for (CartItem item : items) {
            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity().longValue()));
            total = total.add(lineTotal);
            merchantTotals.put(item.getMerchantId(), value(merchantTotals.get(item.getMerchantId())).add(lineTotal));
        }
        normalizeCouponAmounts(coupons, merchantTotals);
        CouponClient.CouponReservation reservation = reservationOrderNo == null
                ? couponClient.preview(buyerId, total, merchantTotals, coupons)
                : couponClient.reserve(reservationOrderNo, buyerId, total, merchantTotals, coupons);
        BigDecimal discount = reservation.getTotalDiscount();
        if (discount.compareTo(total) > 0) {
            throw TradeException.unprocessable("coupon discount cannot exceed order total");
        }
        Map<Long, BigDecimal> freights = new HashMap<Long, BigDecimal>();
        for (Map.Entry<Long, BigDecimal> entry : merchantTotals.entrySet()) freights.put(entry.getKey(), freightFor(entry.getKey(), entry.getValue()));
        return new CheckoutQuote(total, discount, merchantTotals, reservation.getMerchantDiscounts(), freights);
    }

    private List<TradeSubOrder> createSubOrders(String orderNo, List<CartItem> items, Map<Long, BigDecimal> merchantTotals,
                                                Map<Long, BigDecimal> merchantDiscounts, Map<Long, BigDecimal> merchantFreights) {
        List<TradeSubOrder> result = new ArrayList<TradeSubOrder>();
        for (Map.Entry<Long, BigDecimal> entry : merchantTotals.entrySet()) {
            result.add(new TradeSubOrder("SUB-" + UUID.randomUUID().toString(), orderNo, entry.getKey(),
                    entry.getValue(), value(merchantDiscounts.get(entry.getKey())), value(merchantFreights.get(entry.getKey()))));
        }
        return result;
    }

    private List<CartItem> selectedCart(Long buyerId, CheckoutCommand command) {
        if (command == null || command.getCartItemIds() == null || command.getCartItemIds().isEmpty()) {
            throw TradeException.unprocessable("cartItemIds are required");
        }
        Set<Long> requested = new HashSet<Long>(command.getCartItemIds());
        List<CartItem> all = cartItemRepository.findByBuyerIdOrderByIdAsc(buyerId);
        List<CartItem> selected = new ArrayList<CartItem>();
        for (CartItem item : all) {
            if (requested.remove(item.getId())) {
                selected.add(item);
            }
        }
        if (!requested.isEmpty()) {
            throw TradeException.notFound("one or more cart items were not found");
        }
        return selected;
    }

    private void validateCatalogSnapshots(List<CartItem> items) {
        for (CartItem item : items) {
            CatalogClient.CatalogProduct latest = catalogClient.getProduct(item.getProductId());
            if (!item.getMerchantId().equals(latest.getMerchantId())
                    || !item.getProductName().equals(latest.getName())
                    || item.getUnitPrice().compareTo(latest.getPrice()) != 0) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("code", "PRODUCT_CHANGED"); data.put("productId", latest.getProductId());
                data.put("merchantId", latest.getMerchantId()); data.put("name", latest.getName());
                data.put("price", latest.getPrice());
                throw TradeException.conflict("PRODUCT_CHANGED", data);
            }
        }
    }

    private void normalizeCouponAmounts(List<CouponUse> coupons, Map<Long, BigDecimal> merchantTotals) {
        if (coupons == null) {
            return;
        }
        for (CouponUse coupon : coupons) {
            if (coupon != null && coupon.getMerchantId() != null && coupon.getApplicableAmount() == null) {
                coupon.setApplicableAmount(merchantTotals.get(coupon.getMerchantId()));
            }
        }
    }

    private String findSubOrderNo(Collection<TradeSubOrder> subOrders, Long merchantId) {
        for (TradeSubOrder subOrder : subOrders) {
            if (subOrder.getMerchantId().equals(merchantId)) {
                return subOrder.getSubOrderNo();
            }
        }
        throw new IllegalStateException("sub order was not created for merchant");
    }

    private TradeSubOrder requireSubOrder(String subOrderNo) {
        TradeSubOrder subOrder = subOrderRepository.findBySubOrderNo(subOrderNo);
        if (subOrder == null) throw TradeException.notFound("sub order was not found");
        return subOrder;
    }

    private TradeSubOrder requireLockedSubOrder(String subOrderNo) {
        TradeSubOrder subOrder = subOrderRepository.findBySubOrderNoForUpdate(subOrderNo);
        if (subOrder == null) throw TradeException.notFound("sub order was not found");
        return subOrder;
    }

    private void reserveInventory(String orderNo, List<OrderItem> orderItems) {
        Map<String, Integer> quantities = new LinkedHashMap<String, Integer>();
        Map<String, OrderItem> representatives = new LinkedHashMap<String, OrderItem>();
        for (OrderItem item : orderItems) {
            String key = item.getProductId() + ":" + item.getMerchantId();
            Integer existing = quantities.get(key);
            quantities.put(key, (existing == null ? 0 : existing.intValue()) + item.getQuantity().intValue());
            representatives.put(key, item);
        }
        for (Map.Entry<String, OrderItem> entry : representatives.entrySet()) {
            OrderItem item = entry.getValue();
            inventoryClient.reserve(orderNo, item.getProductId(), item.getMerchantId(), quantities.get(entry.getKey()));
        }
    }

    private boolean allToShip(String orderNo) {
        for (TradeSubOrder subOrder : subOrderRepository.findByOrderNo(orderNo)) {
            if (subOrder.getStatus() != SubOrderStatus.TO_SHIP) return false;
        }
        return true;
    }

    private boolean allReceived(String orderNo) {
        for (TradeSubOrder subOrder : subOrderRepository.findByOrderNo(orderNo)) {
            if (subOrder.getStatus() != SubOrderStatus.RECEIVED) return false;
        }
        return true;
    }

    private TradeOrder requireOrder(String orderNo) {
        TradeOrder order = orderRepository.findByOrderNo(orderNo);
        if (order == null) throw TradeException.notFound("order was not found");
        return order;
    }

    private TradeOrder requireLockedOrder(String orderNo) {
        TradeOrder order = orderRepository.findLockedByOrderNo(orderNo);
        if (order == null) throw TradeException.notFound("order was not found");
        return order;
    }

    private TradeOrder requireBuyerOrder(String orderNo, Long buyerId) {
        TradeOrder order = requireOrder(orderNo);
        if (!order.getBuyerId().equals(buyerId)) throw TradeException.forbidden("buyer does not own this order");
        return order;
    }

    private TradeOrder requireBuyerLockedOrder(String orderNo, Long buyerId) {
        TradeOrder order = requireLockedOrder(orderNo);
        if (!order.getBuyerId().equals(buyerId)) throw TradeException.forbidden("buyer does not own this order");
        return order;
    }

    private void event(String orderNo, String subOrderNo, String type, TradeActor actor, String payload) {
        eventRepository.save(new OrderEvent(orderNo, subOrderNo, type, actor.getId(), actor.getType().name(), payload));
        LOG.info("trade_order_event orderNo={} subOrderNo={} type={} actorId={} actorType={}",
                orderNo, subOrderNo, type, actor.getId(), actor.getType());
    }

    private List<com.own.order.dto.OrderTimelineEvent> timeline(List<OrderEvent> events) {
        List<com.own.order.dto.OrderTimelineEvent> result = new ArrayList<com.own.order.dto.OrderTimelineEvent>();
        if (events != null) for (OrderEvent event : events) result.add(com.own.order.dto.OrderTimelineEvent.from(event));
        return result;
    }

    private void lockCart(Long buyerId) {
        cartBuyerGuards.ensure(buyerId);
        if (cartBuyerGuards.findByBuyerIdForUpdate(buyerId) == null) {
            throw new IllegalStateException("cart buyer guard was not created");
        }
    }

    private BigDecimal value(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private void auditSensitiveAddressAccess(TradeActor actor, String action, String orderNo) { sensitiveAccessAuditRepository.save(new com.own.order.domain.SensitiveAccessAudit(actor.getId(), actor.getType().name(), action, "ORDER", orderNo, MDC.get("correlationId"))); }
    private BigDecimal freightFor(Long merchantId, BigDecimal goodsAmount) { com.own.order.domain.MerchantFreightRule rule=freightRuleRepository.findByMerchantId(merchantId); return rule==null?BigDecimal.ZERO:rule.freightFor(goodsAmount); }
    private BigDecimal freightTotal(Map<Long, BigDecimal> freights) { BigDecimal total=BigDecimal.ZERO; for(BigDecimal freight:freights.values()) total=total.add(freight); return total; }
    private AddressClient.AddressSnapshot requireAddress(CheckoutCommand command, Long buyerId) { if(command==null||command.getAddressId()==null) throw TradeException.unprocessable("addressId is required; shippingAddress is no longer accepted"); if(command.getShippingAddress()!=null&&!command.getShippingAddress().trim().isEmpty()) throw TradeException.unprocessable("shippingAddress is no longer accepted; use addressId"); return addressClient.get(buyerId,command.getAddressId()); }
    private boolean blank(String value) { return value == null || value.trim().isEmpty(); }
}
