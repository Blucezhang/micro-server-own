package com.own.inventory.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class InventoryStockTest {

    @Test
    public void reserveCommitAndRefundMaintainNonNegativeStock() {
        InventoryStock stock = new InventoryStock(101L, 201L, 10);
        stock.reserve(4);
        stock.commit(4);
        stock.refund(4);

        assertEquals(Integer.valueOf(10), stock.getAvailableQuantity());
        assertEquals(Integer.valueOf(0), stock.getReservedQuantity());
        assertEquals(Integer.valueOf(0), stock.getSoldQuantity());
    }

    @Test
    public void reserveRejectsOversell() {
        InventoryStock stock = new InventoryStock(101L, 201L, 1);
        try {
            stock.reserve(2);
            fail("expected insufficient inventory");
        } catch (IllegalStateException expected) {
            assertEquals("insufficient inventory", expected.getMessage());
        }
    }

    @Test
    public void reserveOverflowKeepsAvailableAndReservedCountsUnchanged() {
        InventoryStock stock = new InventoryStock(101L, 201L, 1);
        stock.reserve(1);
        stock.setAvailableQuantity(Integer.MAX_VALUE);

        assertThrows(IllegalStateException.class, () -> stock.reserve(Integer.MAX_VALUE));

        balances(stock, Integer.MAX_VALUE, 1, 0);
    }

    @Test
    public void commitOverflowKeepsReservedAndSoldCountsUnchanged() {
        InventoryStock stock = new InventoryStock(101L, 201L, Integer.MAX_VALUE);
        stock.reserve(Integer.MAX_VALUE);
        stock.commit(Integer.MAX_VALUE);
        stock.setAvailableQuantity(1);
        stock.reserve(1);

        assertThrows(IllegalStateException.class, () -> stock.commit(1));

        balances(stock, 0, 1, Integer.MAX_VALUE);
    }

    @Test
    public void releaseOverflowKeepsAvailableAndReservedCountsUnchanged() {
        InventoryStock stock = new InventoryStock(101L, 201L, 1);
        stock.reserve(1);
        stock.setAvailableQuantity(Integer.MAX_VALUE);

        assertThrows(IllegalStateException.class, () -> stock.release(1));

        balances(stock, Integer.MAX_VALUE, 1, 0);
    }

    @Test
    public void refundOverflowKeepsAvailableAndSoldCountsUnchanged() {
        InventoryStock stock = new InventoryStock(101L, 201L, 1);
        stock.reserve(1);
        stock.commit(1);
        stock.setAvailableQuantity(Integer.MAX_VALUE);

        assertThrows(IllegalStateException.class, () -> stock.refund(1));

        balances(stock, Integer.MAX_VALUE, 0, 1);
    }

    private void balances(InventoryStock stock, int available, int reserved, int sold) {
        assertEquals(Integer.valueOf(available), stock.getAvailableQuantity());
        assertEquals(Integer.valueOf(reserved), stock.getReservedQuantity());
        assertEquals(Integer.valueOf(sold), stock.getSoldQuantity());
    }
}
