package com.own.inventory.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.inventory.domain.InventoryAdjustment;
import com.own.inventory.domain.AfterSaleInventoryRefund;
import com.own.inventory.domain.InventoryStock;
import com.own.inventory.domain.InventoryReservation;
import com.own.inventory.dto.AfterSaleInventoryRefundCommand;
import com.own.inventory.dto.AfterSaleInventoryRefundItem;
import com.own.inventory.dto.InventoryAdjustmentCommand;
import com.own.inventory.repository.InventoryAdjustmentRepository;
import com.own.inventory.repository.InventoryReservationRepository;
import com.own.inventory.repository.InventoryStockRepository;
import com.own.inventory.repository.AfterSaleInventoryRefundRepository;
import com.own.inventory.repository.LowStockAlertRuleRepository;
import com.own.inventory.domain.LowStockAlertRule;
import com.own.inventory.dto.LowStockAlertRuleCommand;
import com.own.inventory.dto.LowStockAlert;
import com.own.inventory.dto.InventoryBootstrapItem;
import java.util.Collections;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InventoryServiceAdjustmentTest {
    @Test public void merchantInboundWritesAnAuditableStockAdjustment() {
        InventoryStockRepository stocks = mock(InventoryStockRepository.class); InventoryAdjustmentRepository adjustments = mock(InventoryAdjustmentRepository.class);
        InventoryStock stock = new InventoryStock(101L, 10L, 5); when(stocks.findLockedByProductIdAndMerchantId(101L, 10L)).thenReturn(stock);
        when(adjustments.save(any(InventoryAdjustment.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        InventoryService service = new InventoryService(stocks, mock(InventoryReservationRepository.class), 15, adjustments);

        InventoryAdjustment result = service.adjust(101L, new TradeActor(10L, ActorType.MERCHANT), command("INBOUND", 3));

        assertEquals(Integer.valueOf(8), stock.getAvailableQuantity()); assertEquals(Integer.valueOf(5), result.getBeforeAvailable()); assertEquals(Integer.valueOf(8), result.getAfterAvailable()); verify(stocks).save(stock);
    }

    @Test public void repeatedCatalogueBootstrapKeepsExistingLiveStockInsteadOfResettingIt() {
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        InventoryStock live = new InventoryStock(101L, 10L, 3);
        live.reserve(1); live.commit(1); // original initial inventory has already been sold.
        when(stocks.findByProductIdAndMerchantId(101L, 10L)).thenReturn(live);
        InventoryService service = new InventoryService(stocks, mock(InventoryReservationRepository.class), 15,
                mock(InventoryAdjustmentRepository.class));
        InventoryBootstrapItem item = new InventoryBootstrapItem(); item.setProductId(101L); item.setMerchantId(10L); item.setStocksNum("10");

        InventoryStock returned = service.bootstrap(Collections.singletonList(item)).get(0);

        assertSame(live, returned);
        assertEquals(Integer.valueOf(2), live.getAvailableQuantity());
        assertEquals(Integer.valueOf(1), live.getSoldQuantity());
        verify(stocks).insertIfAbsent(101L, 10L, 10);
        verify(stocks, never()).save(live);
    }

    @Test public void expiryDoesNotReleaseInventoryWhenPaymentConfirmationAlreadyCommittedTheReservation() {
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        InventoryReservationRepository reservations = mock(InventoryReservationRepository.class);
        InventoryReservation committed = new InventoryReservation("RSV-1", "ORD-1", 101L, 10L, 1,
                new Date(System.currentTimeMillis() - 1000L));
        committed.commit();
        when(reservations.findIdsByStatusAndExpiresAtBefore(eq(com.own.inventory.domain.ReservationStatus.RESERVED), any(Date.class)))
                .thenReturn(Collections.singletonList(1L));
        when(reservations.findByIdForUpdate(1L)).thenReturn(committed);
        InventoryService service = new InventoryService(stocks, reservations, 15, mock(InventoryAdjustmentRepository.class));

        assertEquals(0, service.expireReservations());
        verify(stocks, never()).findLockedByProductIdAndMerchantId(anyLong(), anyLong());
        verify(reservations, never()).save(committed);
    }

    @Test public void merchantCannotAdjustAnotherMerchantsStock() {
        InventoryService service = new InventoryService(mock(InventoryStockRepository.class), mock(InventoryReservationRepository.class), 15, mock(InventoryAdjustmentRepository.class));
        try { service.adjust(101L, new TradeActor(10L, ActorType.MERCHANT), command("INBOUND", 1, 20L)); fail("expected validation failure"); }
        catch (TradeException expected) { assertEquals(422, expected.getStatus()); }
    }

    @Test public void repeatedAfterSaleRefundUsesLedgerWithoutRestockingAgain() {
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        InventoryStock stock = new InventoryStock(101L, 10L, 5);
        stock.reserve(2); stock.commit(2);
        when(stocks.findLockedByProductIdAndMerchantId(101L, 10L)).thenReturn(stock);
        AfterSaleInventoryRefundRepository ledger = mock(AfterSaleInventoryRefundRepository.class);
        when(ledger.findByAfterSaleNoAndProductId("AS-1", 101L))
                .thenReturn(new AfterSaleInventoryRefund("AS-1", 101L, 10L, 1));
        InventoryService service = new InventoryService(stocks, mock(InventoryReservationRepository.class), 15,
                mock(InventoryAdjustmentRepository.class), ledger);

        service.refundAfterSale(afterSaleRefundCommand());

        assertEquals(Integer.valueOf(2), stock.getSoldQuantity());
        verify(stocks, never()).save(stock);
        verify(ledger, never()).save(any(AfterSaleInventoryRefund.class));
    }

    @Test public void merchantCanConfigureOwnLowStockRuleAndOnlyBreachedStockIsListed() {
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        InventoryStock lowStock = new InventoryStock(101L, 10L, 2);
        InventoryStock sufficientStock = new InventoryStock(102L, 10L, 8);
        when(stocks.findLockedByProductIdAndMerchantId(101L, 10L)).thenReturn(lowStock);
        when(stocks.findByProductIdAndMerchantId(101L, 10L)).thenReturn(lowStock);
        when(stocks.findByProductIdAndMerchantId(102L, 10L)).thenReturn(sufficientStock);
        LowStockAlertRuleRepository rules = mock(LowStockAlertRuleRepository.class);
        when(rules.save(any(LowStockAlertRule.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        LowStockAlertRule sufficientRule = new LowStockAlertRule(102L, 10L, 5, true);
        when(rules.findByMerchantIdAndEnabledTrueOrderByIdAsc(10L)).thenReturn(Arrays.asList(
                new LowStockAlertRule(101L, 10L, 2, true), sufficientRule));
        InventoryService service = new InventoryService(stocks, mock(InventoryReservationRepository.class), 15,
                mock(InventoryAdjustmentRepository.class), mock(AfterSaleInventoryRefundRepository.class), rules);

        LowStockAlertRule configured = service.configureLowStockAlert(101L,
                new TradeActor(10L, ActorType.MERCHANT), alertRule(3, true));
        List<LowStockAlert> alerts = service.lowStockAlerts(new TradeActor(10L, ActorType.MERCHANT));

        assertEquals(Integer.valueOf(3), configured.getThresholdQuantity());
        assertEquals(1, alerts.size());
        assertEquals(Long.valueOf(101L), alerts.get(0).getProductId());
        verify(rules).save(any(LowStockAlertRule.class));
    }

    @Test public void merchantCannotConfigureLowStockRuleWithoutOwnStock() {
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        when(stocks.findLockedByProductIdAndMerchantId(101L, 10L)).thenReturn(null);
        InventoryService service = new InventoryService(stocks, mock(InventoryReservationRepository.class), 15,
                mock(InventoryAdjustmentRepository.class), mock(AfterSaleInventoryRefundRepository.class),
                mock(LowStockAlertRuleRepository.class));
        try {
            service.configureLowStockAlert(101L, new TradeActor(10L, ActorType.MERCHANT), alertRule(3, true));
            fail("merchant must own an initialized stock row");
        } catch (TradeException expected) {
            assertEquals(404, expected.getStatus());
        }
    }

    @Test public void exactStockReadRequiresOwnerMerchantOrSystem() {
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        InventoryStock stock = new InventoryStock(101L, 10L, 5);
        when(stocks.findByProductIdAndMerchantId(101L, 10L)).thenReturn(stock);
        InventoryService service = new InventoryService(stocks, mock(InventoryReservationRepository.class), 15,
                mock(InventoryAdjustmentRepository.class));

        assertSame(stock, service.getStock(new TradeActor(10L, ActorType.MERCHANT), 101L, 10L));
        assertSame(stock, service.getStock(new TradeActor(1L, ActorType.SYSTEM), 101L, 10L));
        try { service.getStock(new TradeActor(11L, ActorType.MERCHANT), 101L, 10L); fail("other merchant must not read stock"); }
        catch (TradeException expected) { assertEquals(403, expected.getStatus()); }
        try { service.getStock(new TradeActor(2L, ActorType.BUYER), 101L, 10L); fail("buyer must not read exact stock"); }
        catch (TradeException expected) { assertEquals(403, expected.getStatus()); }
    }

    private InventoryAdjustmentCommand command(String type, int quantity) { return command(type, quantity, 10L); }
    private InventoryAdjustmentCommand command(String type, int quantity, Long merchantId) { InventoryAdjustmentCommand value = new InventoryAdjustmentCommand(); value.setMerchantId(merchantId); value.setAdjustmentType(type); value.setQuantity(quantity); value.setReason("test adjustment"); return value; }
    private AfterSaleInventoryRefundCommand afterSaleRefundCommand() { AfterSaleInventoryRefundItem item = new AfterSaleInventoryRefundItem(); item.setProductId(101L); item.setMerchantId(10L); item.setQuantity(1); AfterSaleInventoryRefundCommand command = new AfterSaleInventoryRefundCommand(); command.setAfterSaleNo("AS-1"); command.setItems(Collections.singletonList(item)); return command; }
    private LowStockAlertRuleCommand alertRule(int threshold, boolean enabled) { LowStockAlertRuleCommand command = new LowStockAlertRuleCommand(); command.setThresholdQuantity(threshold); command.setEnabled(enabled); return command; }
}
