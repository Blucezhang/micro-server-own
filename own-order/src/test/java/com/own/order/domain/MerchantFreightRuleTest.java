package com.own.order.domain;

import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MerchantFreightRuleTest {
    @Test public void waivesFreightOnlyAtConfiguredGoodsThreshold() {
        MerchantFreightRule rule = new MerchantFreightRule(10L, new BigDecimal("8.00"), new BigDecimal("99.00"));
        Assertions.assertEquals(new BigDecimal("8.00"), rule.freightFor(new BigDecimal("98.99")));
        Assertions.assertEquals(BigDecimal.ZERO, rule.freightFor(new BigDecimal("99.00")));
    }
}
