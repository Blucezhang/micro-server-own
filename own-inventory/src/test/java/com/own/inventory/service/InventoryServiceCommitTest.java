package com.own.inventory.service;

import com.own.face.trade.TradeException;
import com.own.inventory.domain.InventoryReservation;
import com.own.inventory.domain.InventoryStock;
import com.own.inventory.domain.ReservationStatus;
import com.own.inventory.dto.OrderInventoryItem;
import com.own.inventory.repository.InventoryAdjustmentRepository;
import com.own.inventory.repository.InventoryReservationRepository;
import com.own.inventory.repository.InventoryStockRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventoryServiceCommitTest {

    @Test
    void missingReservationCannotBeSilentlyCommitted() {
        InventoryReservationRepository reservations = mock(InventoryReservationRepository.class);
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        when(reservations.findIdsByOrderNoOrderByIdAsc("ORD-1"))
                .thenReturn(Collections.emptyList());
        InventoryService service = new InventoryService(stocks, reservations, 15,
                mock(InventoryAdjustmentRepository.class));

        assertEquals(409, assertThrows(TradeException.class,
                () -> service.commitOrder("ORD-1", Collections.singletonList(item(101L)))).getStatus());
        verify(stocks, never()).save(any(InventoryStock.class));
    }

    @Test
    void oneExpiredReservationCannotPartiallyCommitAnother() {
        InventoryReservationRepository reservations = mock(InventoryReservationRepository.class);
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        Date later = new Date(System.currentTimeMillis() + 60_000L);
        InventoryReservation live = new InventoryReservation("RSV-1", "ORD-1", 101L, 10L, 1, later);
        InventoryReservation expired = new InventoryReservation("RSV-2", "ORD-1", 102L, 10L, 1,
                new Date(System.currentTimeMillis() - 60_000L));
        when(reservations.findIdsByOrderNoOrderByIdAsc("ORD-1"))
                .thenReturn(Arrays.asList(1L, 2L));
        when(reservations.findByIdForUpdate(1L)).thenReturn(live);
        when(reservations.findByIdForUpdate(2L)).thenReturn(expired);
        InventoryStock stock = new InventoryStock(101L, 10L, 1);
        stock.reserve(1);
        when(stocks.findLockedByProductIdAndMerchantId(101L, 10L)).thenReturn(stock);
        InventoryService service = new InventoryService(stocks, reservations, 15,
                mock(InventoryAdjustmentRepository.class));

        assertEquals(409, assertThrows(TradeException.class,
                () -> service.commitOrder("ORD-1", Arrays.asList(item(101L), item(102L)))).getStatus());
        verify(stocks, never()).save(any(InventoryStock.class));
    }

    @Test
    void committedReservationCanBeReplayedWithoutSellingTwice() {
        InventoryReservationRepository reservations = mock(InventoryReservationRepository.class);
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        InventoryReservation committed = reservation(101L);
        committed.commit();
        when(reservations.findIdsByOrderNoOrderByIdAsc("ORD-1"))
                .thenReturn(Collections.singletonList(1L));
        when(reservations.findByIdForUpdate(1L)).thenReturn(committed);
        InventoryService service = new InventoryService(stocks, reservations, 15,
                mock(InventoryAdjustmentRepository.class));

        service.commitOrder("ORD-1", Collections.singletonList(item(101L)));
        service.commitOrder("ORD-1", Collections.singletonList(item(101L)));

        verify(stocks, never()).save(any(InventoryStock.class));
        verify(reservations, never()).save(any(InventoryReservation.class));
    }

    @Test
    void unexpectedReservationAlsoRejectsCommit() {
        InventoryReservationRepository reservations = mock(InventoryReservationRepository.class);
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        when(reservations.findIdsByOrderNoOrderByIdAsc("ORD-1"))
                .thenReturn(Arrays.asList(1L, 2L));
        when(reservations.findByIdForUpdate(1L)).thenReturn(reservation(101L));
        when(reservations.findByIdForUpdate(2L)).thenReturn(reservation(102L));
        InventoryService service = new InventoryService(stocks, reservations, 15,
                mock(InventoryAdjustmentRepository.class));

        assertEquals(409, assertThrows(TradeException.class,
                () -> service.commitOrder("ORD-1", Collections.singletonList(item(101L)))).getStatus());
        verify(stocks, never()).save(any(InventoryStock.class));
    }

    @Test
    void reservationQuantityMustMatchOrderSnapshot() {
        InventoryReservationRepository reservations = mock(InventoryReservationRepository.class);
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        when(reservations.findIdsByOrderNoOrderByIdAsc("ORD-1"))
                .thenReturn(Collections.singletonList(1L));
        when(reservations.findByIdForUpdate(1L)).thenReturn(reservation(101L));
        InventoryService service = new InventoryService(stocks, reservations, 15,
                mock(InventoryAdjustmentRepository.class));

        assertEquals(409, assertThrows(TradeException.class,
                () -> service.commitOrder("ORD-1", Collections.singletonList(
                        new OrderInventoryItem(101L, 10L, 2)))).getStatus());
        verify(stocks, never()).save(any(InventoryStock.class));
    }

    @Test
    void validReservationCommitsExactlyOnce() {
        InventoryReservationRepository reservations = mock(InventoryReservationRepository.class);
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        InventoryReservation reservation = reservation(101L);
        InventoryStock stock = new InventoryStock(101L, 10L, 1);
        stock.reserve(1);
        when(reservations.findIdsByOrderNoOrderByIdAsc("ORD-1"))
                .thenReturn(Collections.singletonList(1L));
        when(reservations.findByIdForUpdate(1L)).thenReturn(reservation);
        when(stocks.findLockedByProductIdAndMerchantId(101L, 10L)).thenReturn(stock);
        InventoryService service = new InventoryService(stocks, reservations, 15,
                mock(InventoryAdjustmentRepository.class));

        service.commitOrder("ORD-1", Collections.singletonList(item(101L)));
        service.commitOrder("ORD-1", Collections.singletonList(item(101L)));

        assertEquals(ReservationStatus.COMMITTED, reservation.getStatus());
        assertEquals(Integer.valueOf(1), stock.getSoldQuantity());
        verify(stocks, times(1)).save(stock);
    }

    private OrderInventoryItem item(Long productId) {
        return new OrderInventoryItem(productId, 10L, 1);
    }

    private InventoryReservation reservation(Long productId) {
        return new InventoryReservation("RSV-" + productId, "ORD-1", productId, 10L, 1,
                new Date(System.currentTimeMillis() + 60_000L));
    }
}
