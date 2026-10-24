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

class InventoryServiceRefundTest {
    @Test
    void missingCommittedReservationCannotBeSilentlyRefunded() {
        InventoryService service = new InventoryService(mock(InventoryStockRepository.class),
                mock(InventoryReservationRepository.class), 15, mock(InventoryAdjustmentRepository.class));

        assertEquals(409, assertThrows(TradeException.class,
                () -> service.refundOrder("ORD-1", Collections.singletonList(item(101L)))).getStatus());
    }

    @Test
    void oneReleasedReservationCannotPartiallyRefundCommittedStock() {
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        InventoryReservationRepository reservations = mock(InventoryReservationRepository.class);
        InventoryReservation committed = reservation(101L);
        committed.commit();
        InventoryReservation released = reservation(102L);
        released.release();
        when(reservations.findIdsByOrderNoOrderByIdAsc("ORD-1")).thenReturn(Arrays.asList(1L, 2L));
        when(reservations.findByIdForUpdate(1L)).thenReturn(committed);
        when(reservations.findByIdForUpdate(2L)).thenReturn(released);
        InventoryService service = service(stocks, reservations);

        assertEquals(409, assertThrows(TradeException.class,
                () -> service.refundOrder("ORD-1", Arrays.asList(item(101L), item(102L)))).getStatus());
        verify(stocks, never()).save(any(InventoryStock.class));
        assertEquals(ReservationStatus.COMMITTED, committed.getStatus());
    }

    @Test
    void fullRefundReplayDoesNotReturnInventoryTwice() {
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        InventoryReservationRepository reservations = mock(InventoryReservationRepository.class);
        InventoryReservation committed = reservation(101L);
        committed.commit();
        InventoryStock stock = new InventoryStock(101L, 10L, 1);
        stock.reserve(1);
        stock.commit(1);
        when(reservations.findIdsByOrderNoOrderByIdAsc("ORD-1")).thenReturn(Collections.singletonList(1L));
        when(reservations.findByIdForUpdate(1L)).thenReturn(committed);
        when(stocks.findLockedByProductIdAndMerchantId(101L, 10L)).thenReturn(stock);
        InventoryService service = service(stocks, reservations);

        service.refundOrder("ORD-1", Collections.singletonList(item(101L)));
        service.refundOrder("ORD-1", Collections.singletonList(item(101L)));

        assertEquals(ReservationStatus.REFUNDED, committed.getStatus());
        assertEquals(Integer.valueOf(1), stock.getAvailableQuantity());
        assertEquals(Integer.valueOf(0), stock.getSoldQuantity());
        verify(stocks, times(1)).save(stock);
    }

    @Test
    void refundQuantityOverflowIsAConflictWithoutInventoryOrReservationWrite() {
        InventoryStockRepository stocks = mock(InventoryStockRepository.class);
        InventoryReservationRepository reservations = mock(InventoryReservationRepository.class);
        InventoryReservation committed = reservation(101L);
        committed.commit();
        InventoryStock stock = new InventoryStock(101L, 10L, 1);
        stock.reserve(1);
        stock.commit(1);
        stock.setAvailableQuantity(Integer.MAX_VALUE);
        when(reservations.findIdsByOrderNoOrderByIdAsc("ORD-1")).thenReturn(Collections.singletonList(1L));
        when(reservations.findByIdForUpdate(1L)).thenReturn(committed);
        when(stocks.findLockedByProductIdAndMerchantId(101L, 10L)).thenReturn(stock);

        TradeException error = assertThrows(TradeException.class, () -> service(stocks, reservations)
                .refundOrder("ORD-1", Collections.singletonList(item(101L))));

        assertEquals(409, error.getStatus());
        assertEquals("inventory quantity exceeds supported range", error.getMessage());
        assertEquals(ReservationStatus.COMMITTED, committed.getStatus());
        verify(stocks, never()).save(any(InventoryStock.class));
        verify(reservations, never()).save(any(InventoryReservation.class));
    }

    private InventoryService service(InventoryStockRepository stocks, InventoryReservationRepository reservations) {
        return new InventoryService(stocks, reservations, 15, mock(InventoryAdjustmentRepository.class));
    }

    private OrderInventoryItem item(Long productId) {
        return new OrderInventoryItem(productId, 10L, 1);
    }

    private InventoryReservation reservation(Long productId) {
        return new InventoryReservation("RSV-" + productId, "ORD-1", productId, 10L, 1,
                new Date(System.currentTimeMillis() + 60_000L));
    }
}
