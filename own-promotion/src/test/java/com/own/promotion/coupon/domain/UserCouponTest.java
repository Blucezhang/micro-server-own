package com.own.promotion.coupon.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import org.junit.jupiter.api.Test;

public class UserCouponTest {

    @Test
    public void refundedCouponReturnsToAvailableBeforeExpiry() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        UserCoupon coupon = new UserCoupon("CPN-1", 1L, CouponType.MALL, 7L, null,
                BigDecimal.TEN, BigDecimal.ONE, calendar.getTime());
        coupon.reserve("ORD-1", new Date(System.currentTimeMillis() + 60_000L));
        coupon.consume();
        coupon.restoreAfterRefund(new Date());
        assertEquals(CouponStatus.AVAILABLE, coupon.getStatus());
        assertEquals(null, coupon.getReservedOrderNo());
    }

    @Test
    public void expiredReservationReturnsCouponToAvailable() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        UserCoupon coupon = new UserCoupon("CPN-2", 1L, CouponType.MALL, 7L, null,
                BigDecimal.TEN, BigDecimal.ONE, calendar.getTime());
        Date expiredHold = new Date(System.currentTimeMillis() - 1_000L);
        coupon.reserve("ORD-2", expiredHold);
        coupon.release(new Date());

        assertEquals(CouponStatus.AVAILABLE, coupon.getStatus());
        assertEquals(null, coupon.getReservedOrderNo());
        assertEquals(null, coupon.getReservationExpiresAt());
    }
}
