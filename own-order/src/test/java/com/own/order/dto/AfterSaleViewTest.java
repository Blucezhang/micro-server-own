package com.own.order.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.own.order.domain.AfterSale;
import com.own.order.domain.AfterSaleType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

public class AfterSaleViewTest {
    @Test
    public void participantViewDoesNotSerializePersistentBuyerOrMerchantIds() throws Exception {
        AfterSale sale = new AfterSale("AS-1", "ORD-1", "SUB-1", 101L, 202L,
                AfterSaleType.REFUND_ONLY, BigDecimal.TEN, "wrong size");
        String json = new ObjectMapper().writeValueAsString(new AfterSaleView(sale));

        assertTrue(json.contains("AS-1"));
        assertFalse(json.contains("buyerId"));
        assertFalse(json.contains("merchantId"));
        assertFalse(json.contains("101"));
        assertFalse(json.contains("202"));
    }
}
