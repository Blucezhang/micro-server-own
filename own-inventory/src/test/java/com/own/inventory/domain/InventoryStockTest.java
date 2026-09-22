package com.own.inventory.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
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
}
