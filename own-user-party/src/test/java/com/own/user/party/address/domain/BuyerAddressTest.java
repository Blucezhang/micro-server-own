package com.own.user.party.address.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BuyerAddressTest {
    @Test public void formatsAStableAddressSnapshot() {
        BuyerAddress address = new BuyerAddress(1L, "张三", "13800138000", "上海市", "上海市", "浦东新区", "示例路1号", true);
        Assertions.assertEquals("上海市上海市浦东新区示例路1号", address.formatted());
        Assertions.assertTrue(address.getDefaultAddress());
    }
}
