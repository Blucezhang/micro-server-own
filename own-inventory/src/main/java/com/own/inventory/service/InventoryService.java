package com.own.inventory.service;

import com.own.face.trade.config.ReservationLimits;
import com.own.face.trade.TradeException;
import com.own.inventory.domain.InventoryReservation;
import com.own.inventory.domain.InventoryStock;
import com.own.inventory.domain.ReservationStatus;
import com.own.inventory.dto.InventoryBootstrapItem;
import com.own.inventory.dto.ReservationCommand;
import com.own.inventory.dto.StockCommand;
import com.own.inventory.dto.InventoryAdjustmentCommand;
import com.own.inventory.dto.OrderInventoryItem;
import com.own.inventory.domain.InventoryAdjustment;
import com.own.inventory.domain.AfterSaleInventoryRefund;
import com.own.inventory.domain.LowStockAlertRule;
import com.own.inventory.repository.InventoryAdjustmentRepository;
import com.own.inventory.repository.AfterSaleInventoryRefundRepository;
import com.own.inventory.repository.LowStockAlertRuleRepository;
import com.own.inventory.dto.AfterSaleInventoryRefundCommand;
import com.own.inventory.dto.AfterSaleInventoryRefundItem;
import com.own.inventory.dto.LowStockAlert;
import com.own.inventory.dto.LowStockAlertRuleCommand;
import com.own.face.trade.TradeActor;
import com.own.face.trade.ActorType;
import com.own.inventory.repository.InventoryReservationRepository;
import com.own.inventory.repository.InventoryStockRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

@Service
public class InventoryService {


    private final InventoryStockRepository stockRepository;
    private final InventoryReservationRepository reservationRepository;
    private final int reservationTtlMinutes;
    private final InventoryAdjustmentRepository adjustmentRepository;
    private final AfterSaleInventoryRefundRepository afterSaleRefundRepository;
    private final LowStockAlertRuleRepository lowStockAlertRuleRepository;
    @org.springframework.beans.factory.annotation.Autowired(required = false) private com.own.inventory.repository.AfterSaleInventoryExchangeRepository afterSaleExchangeRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public InventoryService(InventoryStockRepository stockRepository,
                            InventoryReservationRepository reservationRepository,
                            @Value("${trade.inventory.reservation-ttl-minutes}") int reservationTtlMinutes,
                            InventoryAdjustmentRepository adjustmentRepository, AfterSaleInventoryRefundRepository afterSaleRefundRepository,
                            LowStockAlertRuleRepository lowStockAlertRuleRepository) {
        this.stockRepository = stockRepository;
        this.reservationRepository = reservationRepository;
        this.reservationTtlMinutes = reservationTtlMinutes;
        this.adjustmentRepository = adjustmentRepository;
        this.afterSaleRefundRepository = afterSaleRefundRepository;
        this.lowStockAlertRuleRepository = lowStockAlertRuleRepository;
    }

    /** Compatibility constructor retained for focused unit tests of legacy stock operations. */
    public InventoryService(InventoryStockRepository stockRepository,
                            InventoryReservationRepository reservationRepository,
                            int reservationTtlMinutes,
                            InventoryAdjustmentRepository adjustmentRepository) {
        this(stockRepository, reservationRepository, reservationTtlMinutes, adjustmentRepository, null, null);
    }

    /** Compatibility constructor retained for focused after-sale inventory tests. */
    public InventoryService(InventoryStockRepository stockRepository,
                            InventoryReservationRepository reservationRepository,
                            int reservationTtlMinutes,
                            InventoryAdjustmentRepository adjustmentRepository,
                            AfterSaleInventoryRefundRepository afterSaleRefundRepository) {
        this(stockRepository, reservationRepository, reservationTtlMinutes, adjustmentRepository, afterSaleRefundRepository, null);
    }

    @Transactional
    public InventoryStock setAvailableStock(Long productId, StockCommand command) {
        requirePositive(productId, "productId");
        requirePositive(command == null ? null : command.getMerchantId(), "merchantId");
        Integer quantity = command.getAvailableQuantity();
        if (quantity == null || quantity.intValue() < 0) {
            throw TradeException.unprocessable("availableQuantity must be zero or greater");
        }
        InventoryStock stock = stockRepository.findLockedByProductIdAndMerchantId(productId, command.getMerchantId());
        if (stock == null) {
            return stockRepository.save(new InventoryStock(productId, command.getMerchantId(), quantity));
        }
        stock.setAvailableQuantity(quantity);
        return stockRepository.save(stock);
    }

    @Transactional
    public List<InventoryStock> bootstrap(List<InventoryBootstrapItem> products) {
        if (products == null || products.isEmpty()) {
            throw TradeException.badRequest("products are required");
        }
        List<InventoryStock> stocks = new ArrayList<InventoryStock>();
        for (InventoryBootstrapItem item : products) {
            if (item == null) throw TradeException.unprocessable("product item is required");
            requirePositive(item.getProductId(), "productId");
            requirePositive(item.getMerchantId(), "merchantId");
            int quantity = parseQuantity(item.getStocksNum());
            stockRepository.insertIfAbsent(item.getProductId(), item.getMerchantId(), quantity);
            InventoryStock stock = stockRepository.findByProductIdAndMerchantId(item.getProductId(), item.getMerchantId());
            if (stock == null) throw new IllegalStateException("initialized inventory stock was not found");
            stocks.add(stock);
        }
        return stocks;
    }

    @Transactional
    public InventoryReservation reserve(ReservationCommand command) {
        validateReservation(command);
        InventoryStock stock = stockRepository.findLockedByProductIdAndMerchantId(
                command.getProductId(), command.getMerchantId());
        if (stock == null) {
            throw TradeException.notFound("inventory stock was not initialized");
        }
        // The stock lock serializes competing create requests for the same SKU.
        // Re-check only after acquiring it so a duplicate order/SKU request does
        // not consume stock or race the database unique key.
        InventoryReservation existing = reservationRepository.findByOrderNoAndProductId(
                command.getOrderNo(), command.getProductId());
        if (existing != null) {
            if (existing.getMerchantId().equals(command.getMerchantId())
                    && existing.getQuantity().equals(command.getQuantity())) return existing;
            throw TradeException.conflict("an incompatible reservation already exists for this order item");
        }
        try {
            stock.reserve(command.getQuantity().intValue());
        } catch (IllegalStateException exception) {
            throw TradeException.conflict(exception.getMessage());
        }
        stockRepository.save(stock);
        int ttl = command.getTtlMinutes() == null ? reservationTtlMinutes : command.getTtlMinutes();
        if (ttl <= 0 || ttl > ReservationLimits.MAX_TTL_MINUTES) {
            throw TradeException.unprocessable("ttlMinutes must be between 1 and " + ReservationLimits.MAX_TTL_MINUTES);
        }
        InventoryReservation reservation = new InventoryReservation(
                "RSV-" + UUID.randomUUID().toString(), command.getOrderNo(), command.getProductId(),
                command.getMerchantId(), command.getQuantity(), new Date(System.currentTimeMillis() + java.time.Duration.ofMinutes(ttl).toMillis()));
        return reservationRepository.save(reservation);
    }

    @Transactional
    public void commitOrder(String orderNo, List<OrderInventoryItem> items) {
        List<InventoryReservation> reservations = lockedMatchingOrderReservations(orderNo, items);
        List<InventoryReservation> toCommit = new ArrayList<InventoryReservation>();
        Date now = new Date();
        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.COMMITTED) continue;
            if (reservation.getStatus() != ReservationStatus.RESERVED || reservation.getExpiresAt() == null
                    || !reservation.getExpiresAt().after(now)) {
                throw TradeException.conflict("inventory reservation is no longer active");
            }
            toCommit.add(reservation);
        }
        for (InventoryReservation reservation : toCommit) commitReservation(reservation);
    }

    @Transactional
    public void releaseOrder(String orderNo) {
        changeReservationState(orderNo);
    }

    @Transactional
    public void refundOrder(String orderNo, List<OrderInventoryItem> items) {
        List<InventoryReservation> reservations = lockedMatchingOrderReservations(orderNo, items);
        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() != ReservationStatus.COMMITTED
                    && reservation.getStatus() != ReservationStatus.REFUNDED) {
                throw TradeException.conflict("inventory reservation was not committed");
            }
        }
        for (InventoryReservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.REFUNDED) continue;
            InventoryStock stock = lockedStock(reservation.getProductId(), reservation.getMerchantId());
            try {
                stock.refund(reservation.getQuantity().intValue());
            } catch (IllegalStateException exception) {
                throw TradeException.conflict(exception.getMessage());
            }
            stockRepository.save(stock);
            reservation.refund();
            reservationRepository.save(reservation);
        }
    }

    private List<InventoryReservation> lockedMatchingOrderReservations(String orderNo, List<OrderInventoryItem> items) {
        if (orderNo == null || orderNo.trim().isEmpty() || items == null || items.isEmpty()) {
            throw TradeException.badRequest("orderNo and inventory items are required");
        }
        Map<Long, OrderInventoryItem> expected = new LinkedHashMap<Long, OrderInventoryItem>();
        for (OrderInventoryItem item : items) {
            requirePositive(item == null ? null : item.getProductId(), "productId");
            requirePositive(item.getMerchantId(), "merchantId");
            if (item.getQuantity() == null || item.getQuantity().intValue() <= 0) {
                throw TradeException.unprocessable("quantity must be positive");
            }
            if (expected.putIfAbsent(item.getProductId(), item) != null) {
                throw TradeException.unprocessable("duplicate productId in inventory items");
            }
        }
        List<InventoryReservation> reservations = new ArrayList<InventoryReservation>();
        for (Long id : reservationRepository.findIdsByOrderNoOrderByIdAsc(orderNo)) {
            InventoryReservation reservation = reservationRepository.findByIdForUpdate(id);
            if (reservation == null || !orderNo.equals(reservation.getOrderNo())) {
                throw TradeException.conflict("inventory reservation does not match order");
            }
            OrderInventoryItem item = expected.remove(reservation.getProductId());
            if (item == null || !item.getMerchantId().equals(reservation.getMerchantId())
                    || !item.getQuantity().equals(reservation.getQuantity())) {
                throw TradeException.conflict("inventory reservation does not match order items");
            }
            reservations.add(reservation);
        }
        if (!expected.isEmpty()) throw TradeException.conflict("inventory reservation is missing for order items");
        return reservations;
    }

    @Transactional
    public int expireReservations() {
        Date now = new Date();
        int released = 0;
        for (Long reservationId : reservationRepository.findIdsByStatusAndExpiresAtBefore(ReservationStatus.RESERVED, now)) {
            InventoryReservation reservation = reservationRepository.findByIdForUpdate(reservationId);
            // Confirmation and expiry both lock this row. A payment confirmation
            // that won the race changes the status before this check.
            if (reservation == null || reservation.getStatus() != ReservationStatus.RESERVED
                    || !reservation.getExpiresAt().before(now)) continue;
            releaseReservation(reservation, true);
            released++;
        }
        return released;
    }

    @Transactional
    public void refundAfterSale(AfterSaleInventoryRefundCommand command) {
        if (afterSaleRefundRepository == null) throw new IllegalStateException("after-sale refund ledger is not configured");
        if (command == null || command.getAfterSaleNo() == null || command.getAfterSaleNo().trim().isEmpty() || command.getItems() == null || command.getItems().isEmpty()) throw TradeException.unprocessable("afterSaleNo and refund items are required");
        Map<String, AfterSaleInventoryRefundItem> merged = new LinkedHashMap<String, AfterSaleInventoryRefundItem>();
        for (AfterSaleInventoryRefundItem item : command.getItems()) {
            requirePositive(item == null ? null : item.getProductId(), "productId"); requirePositive(item == null ? null : item.getMerchantId(), "merchantId"); if (item.getQuantity() == null || item.getQuantity().intValue() <= 0) throw TradeException.unprocessable("quantity must be positive");
            String key = item.getProductId() + ":" + item.getMerchantId(); AfterSaleInventoryRefundItem target = merged.get(key);
            if (target == null) { target = new AfterSaleInventoryRefundItem(); target.setProductId(item.getProductId()); target.setMerchantId(item.getMerchantId()); target.setQuantity(item.getQuantity()); merged.put(key, target); } else target.setQuantity(target.getQuantity() + item.getQuantity());
        }
        for (AfterSaleInventoryRefundItem item : merged.values()) {
            // The stock row is the serialisation point for both normal stock changes and
            // duplicate after-sale callbacks. Re-read the ledger only after it is locked.
            InventoryStock stock = lockedStock(item.getProductId(), item.getMerchantId());
            AfterSaleInventoryRefund existing = afterSaleRefundRepository.findByAfterSaleNoAndProductId(command.getAfterSaleNo(), item.getProductId());
            if (existing != null) { if (!existing.getMerchantId().equals(item.getMerchantId()) || !existing.getQuantity().equals(item.getQuantity())) throw TradeException.conflict("after-sale inventory refund does not match original request"); continue; }
            try { stock.refund(item.getQuantity()); } catch (IllegalStateException exception) { throw TradeException.conflict(exception.getMessage()); }
            stockRepository.save(stock); afterSaleRefundRepository.save(new AfterSaleInventoryRefund(command.getAfterSaleNo(), item.getProductId(), item.getMerchantId(), item.getQuantity()));
        }
    }

    @Transactional
    public void exchangeAfterSale(AfterSaleInventoryRefundCommand command) {
        if (afterSaleExchangeRepository == null) throw new IllegalStateException("after-sale exchange ledger is not configured");
        if (command == null || command.getAfterSaleNo() == null || command.getItems() == null || command.getItems().isEmpty()) throw TradeException.unprocessable("afterSaleNo and exchange items are required");
        Map<String, AfterSaleInventoryRefundItem> merged = new LinkedHashMap<String, AfterSaleInventoryRefundItem>();
        for (AfterSaleInventoryRefundItem item : command.getItems()) {
            requirePositive(item == null ? null : item.getProductId(), "productId"); requirePositive(item == null ? null : item.getMerchantId(), "merchantId");
            if (item.getQuantity() == null || item.getQuantity() <= 0) throw TradeException.unprocessable("quantity must be positive");
            String key = item.getProductId() + ":" + item.getMerchantId(); AfterSaleInventoryRefundItem target = merged.get(key);
            if (target == null) { target = new AfterSaleInventoryRefundItem(); target.setProductId(item.getProductId()); target.setMerchantId(item.getMerchantId()); target.setQuantity(item.getQuantity()); merged.put(key, target); }
            else target.setQuantity(target.getQuantity() + item.getQuantity());
        }
        for (AfterSaleInventoryRefundItem item : merged.values()) {
            InventoryStock stock = lockedStock(item.getProductId(), item.getMerchantId());
            com.own.inventory.domain.AfterSaleInventoryExchange existing = afterSaleExchangeRepository.findByAfterSaleNoAndProductId(command.getAfterSaleNo(), item.getProductId());
            if (existing != null) { if (!existing.getMerchantId().equals(item.getMerchantId()) || !existing.getQuantity().equals(item.getQuantity())) throw TradeException.conflict("after-sale exchange does not match original request"); continue; }
            try { stock.exchange(item.getQuantity()); } catch (IllegalStateException exception) { throw TradeException.conflict("sold inventory is insufficient for after-sale exchange"); }
            stockRepository.save(stock); afterSaleExchangeRepository.save(new com.own.inventory.domain.AfterSaleInventoryExchange(command.getAfterSaleNo(), item.getProductId(), item.getMerchantId(), item.getQuantity()));
        }
    }

    public InventoryStock getStock(TradeActor actor, Long productId, Long merchantId) {
        if (actor == null || actor.getType() != ActorType.SYSTEM && actor.getType() != ActorType.MERCHANT) {
            throw TradeException.forbidden("merchant or system actor is required to read inventory");
        }
        if (actor.getType() == ActorType.MERCHANT && !actor.getId().equals(merchantId)) {
            throw TradeException.forbidden("merchant does not own this inventory");
        }
        InventoryStock stock = stockRepository.findByProductIdAndMerchantId(productId, merchantId);
        if (stock == null) {
            throw TradeException.notFound("inventory stock was not found");
        }
        return stock;
    }

    @Transactional
    public InventoryAdjustment adjust(Long productId, TradeActor actor, InventoryAdjustmentCommand command) {
        actor.require(ActorType.MERCHANT); requirePositive(productId, "productId");
        if(command==null||command.getMerchantId()==null||!actor.getId().equals(command.getMerchantId())||command.getQuantity()==null||command.getQuantity().intValue()<0||command.getAdjustmentType()==null||command.getReason()==null||command.getReason().trim().isEmpty()) throw TradeException.unprocessable("merchantId, adjustmentType, nonnegative quantity and reason are required");
        String type=command.getAdjustmentType().trim(); if(!"INBOUND".equals(type)&&!"OUTBOUND".equals(type)&&!"STOCKTAKE".equals(type))throw TradeException.unprocessable("adjustmentType must be INBOUND, OUTBOUND or STOCKTAKE");
        InventoryStock stock=lockedStock(productId,actor.getId()); int before=stock.getAvailableQuantity(); int after="INBOUND".equals(type)?before+command.getQuantity():"OUTBOUND".equals(type)?before-command.getQuantity():command.getQuantity();
        if(after<0)throw TradeException.conflict("inventory cannot be negative");
        stock.setAvailableQuantity(after); stockRepository.save(stock);
        return adjustmentRepository.save(new InventoryAdjustment(productId,actor.getId(),type,command.getQuantity(),before,after,command.getReason().trim(),actor.getId()));
    }

    @Transactional
    public LowStockAlertRule configureLowStockAlert(Long productId, TradeActor actor, LowStockAlertRuleCommand command) {
        actor.require(ActorType.MERCHANT);
        requirePositive(productId, "productId");
        if (command == null || command.getThresholdQuantity() == null || command.getThresholdQuantity().intValue() < 0
                || command.getEnabled() == null) {
            throw TradeException.unprocessable("nonnegative thresholdQuantity and enabled are required");
        }
        // The stock-row lock both proves merchant ownership and serializes initial rule creation.
        lockedStock(productId, actor.getId());
        if (lowStockAlertRuleRepository == null) throw new IllegalStateException("low-stock alert rules are not configured");
        LowStockAlertRule rule = lowStockAlertRuleRepository.findByProductIdAndMerchantIdForUpdate(productId, actor.getId());
        if (rule == null) {
            rule = new LowStockAlertRule(productId, actor.getId(), command.getThresholdQuantity(), command.getEnabled());
        } else {
            rule.update(command.getThresholdQuantity(), command.getEnabled());
        }
        return lowStockAlertRuleRepository.save(rule);
    }

    public List<LowStockAlert> lowStockAlerts(TradeActor actor) {
        actor.require(ActorType.MERCHANT);
        return currentLowStockAlerts(lowStockAlertRuleRepository == null
                ? java.util.Collections.<LowStockAlertRule>emptyList()
                : lowStockAlertRuleRepository.findByMerchantIdAndEnabledTrueOrderByIdAsc(actor.getId()));
    }

    public List<LowStockAlert> allLowStockAlerts(TradeActor actor) {
        actor.require(ActorType.SYSTEM);
        return currentLowStockAlerts(lowStockAlertRuleRepository == null
                ? java.util.Collections.<LowStockAlertRule>emptyList()
                : lowStockAlertRuleRepository.findByEnabledTrueOrderByIdAsc());
    }

    private List<LowStockAlert> currentLowStockAlerts(List<LowStockAlertRule> rules) {
        List<LowStockAlert> alerts = new ArrayList<LowStockAlert>();
        for (LowStockAlertRule rule : rules) {
            InventoryStock stock = stockRepository.findByProductIdAndMerchantId(rule.getProductId(), rule.getMerchantId());
            if (stock != null && stock.getAvailableQuantity().intValue() <= rule.getThresholdQuantity().intValue()) {
                alerts.add(new LowStockAlert(rule, stock));
            }
        }
        return alerts;
    }

    private void changeReservationState(String orderNo) {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            throw TradeException.badRequest("orderNo is required");
        }
        for (Long reservationId : reservationRepository.findIdsByOrderNoAndStatus(orderNo, ReservationStatus.RESERVED)) {
            InventoryReservation reservation = reservationRepository.findByIdForUpdate(reservationId);
            if (reservation == null || reservation.getStatus() != ReservationStatus.RESERVED) continue;
            releaseReservation(reservation, false);
        }
    }

    private void commitReservation(InventoryReservation reservation) {
        InventoryStock stock = lockedStock(reservation.getProductId(), reservation.getMerchantId());
        try {
            stock.commit(reservation.getQuantity().intValue());
        } catch (IllegalStateException exception) {
            throw TradeException.conflict(exception.getMessage());
        }
        stockRepository.save(stock);
        reservation.commit();
        reservationRepository.save(reservation);
    }

    private void releaseReservation(InventoryReservation reservation, boolean expired) {
        InventoryStock stock = lockedStock(reservation.getProductId(), reservation.getMerchantId());
        try {
            stock.release(reservation.getQuantity().intValue());
        } catch (IllegalStateException exception) {
            throw TradeException.conflict(exception.getMessage());
        }
        stockRepository.save(stock);
        if (expired) {
            reservation.expire();
        } else {
            reservation.release();
        }
        reservationRepository.save(reservation);
    }

    private void validateReservation(ReservationCommand command) {
        if (command == null) {
            throw TradeException.badRequest("reservation request is required");
        }
        if (command.getOrderNo() == null || command.getOrderNo().trim().isEmpty()) {
            throw TradeException.badRequest("orderNo is required");
        }
        requirePositive(command.getProductId(), "productId");
        requirePositive(command.getMerchantId(), "merchantId");
        if (command.getQuantity() == null || command.getQuantity().intValue() <= 0) {
            throw TradeException.unprocessable("quantity must be positive");
        }
    }

    private void requirePositive(Long value, String name) {
        if (value == null || value.longValue() <= 0L) {
            throw TradeException.unprocessable(name + " must be positive");
        }
    }

    private int parseQuantity(String value) {
        try {
            int quantity = Integer.parseInt(value);
            if (quantity < 0) {
                throw TradeException.unprocessable("stocksNum must be zero or greater");
            }
            return quantity;
        } catch (NumberFormatException exception) {
            throw TradeException.unprocessable("stocksNum must be an integer");
        }
    }

    private InventoryStock lockedStock(Long productId, Long merchantId) {
        InventoryStock stock = stockRepository.findLockedByProductIdAndMerchantId(productId, merchantId);
        if (stock == null) throw TradeException.notFound("inventory stock was not found");
        return stock;
    }
}
