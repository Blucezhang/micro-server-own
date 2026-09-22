package com.own.promotion.coupon.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.own.face.trade.TradeException;
import com.own.promotion.coupon.domain.CouponStock;
import com.own.promotion.coupon.domain.UserCoupon;
import com.own.promotion.coupon.domain.CouponStatus;
import com.own.promotion.coupon.domain.MerchantCouponTemplate;
import com.own.promotion.coupon.dto.CouponReservationCommand;
import com.own.promotion.coupon.dto.CouponUse;
import com.own.promotion.coupon.dto.ClaimCouponCommand;
import com.own.promotion.coupon.repository.CouponStockRepository;
import com.own.promotion.coupon.repository.UserCouponRepository;
import com.own.promotion.coupon.repository.MerchantCouponTemplateRepository;
import com.own.promotion.dao.MallTicketDao;
import com.own.promotion.dao.StoreTicketDao;
import com.own.promotion.dao.domain.MallTicket;
import java.util.Calendar;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class CouponServiceClaimTest {

    @Test
    public void claimInitializesThenLocksSharedStockBeforeDecrementing() {
        CouponStockRepository stocks = mock(CouponStockRepository.class);
        CouponStock stock = new CouponStock("MALL:7:0", 1);
        when(stocks.findByScopeKeyForUpdate("MALL:7:0")).thenReturn(stock);
        UserCouponRepository coupons = mock(UserCouponRepository.class);
        when(coupons.save(any(UserCoupon.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        MallTicketDao tickets = mock(MallTicketDao.class);
        when(tickets.getFromId(7)).thenReturn(ticket());
        CouponService service = new CouponService(stocks, coupons, tickets, mock(StoreTicketDao.class), 15);

        UserCoupon claimed = service.claim(1L, claimCommand());

        assertNotNull(claimed);
        verify(stocks).insertIfAbsent("MALL:7:0", 1);
        verify(stocks).findByScopeKeyForUpdate("MALL:7:0");
        verify(stocks).save(stock);
    }

    @Test
    public void exhaustedLockedStockReturnsBusinessConflict() {
        CouponStockRepository stocks = mock(CouponStockRepository.class);
        CouponStock stock = new CouponStock("MALL:7:0", 1);
        when(stocks.findByScopeKeyForUpdate("MALL:7:0")).thenReturn(stock);
        UserCouponRepository coupons = mock(UserCouponRepository.class);
        when(coupons.save(any(UserCoupon.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        MallTicketDao tickets = mock(MallTicketDao.class);
        when(tickets.getFromId(7)).thenReturn(ticket());
        CouponService service = new CouponService(stocks, coupons, tickets, mock(StoreTicketDao.class), 15);
        service.claim(1L, claimCommand());

        try {
            service.claim(2L, claimCommand());
            fail("exhausted coupon stock must become a conflict");
        } catch (TradeException expected) {
            org.junit.jupiter.api.Assertions.assertEquals(409, expected.getStatus());
        }
    }

    @Test
    public void reservationLocksTheCouponBeforeChangingItsState() {
        CouponStockRepository stocks = mock(CouponStockRepository.class);
        UserCouponRepository coupons = mock(UserCouponRepository.class);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        UserCoupon coupon = new UserCoupon("CPN-1", 1L, com.own.promotion.coupon.domain.CouponType.MALL,
                7L, null, BigDecimal.ZERO, BigDecimal.ONE, calendar.getTime());
        when(coupons.findByCouponNoForUpdate("CPN-1")).thenReturn(coupon);
        CouponService service = new CouponService(stocks, coupons, mock(MallTicketDao.class), mock(StoreTicketDao.class), 15);

        service.reserve(reservationCommand());

        org.junit.jupiter.api.Assertions.assertEquals(CouponStatus.RESERVED, coupon.getStatus());
        verify(coupons).findByCouponNoForUpdate("CPN-1");
    }

    @Test
    public void previewCalculatesAvailableCouponWithoutReservingIt() {
        CouponStockRepository stocks = mock(CouponStockRepository.class);
        UserCouponRepository coupons = mock(UserCouponRepository.class);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        UserCoupon coupon = new UserCoupon("CPN-1", 1L, com.own.promotion.coupon.domain.CouponType.MALL,
                7L, null, BigDecimal.ZERO, BigDecimal.ONE, calendar.getTime());
        when(coupons.findByCouponNo("CPN-1")).thenReturn(coupon);
        CouponService service = new CouponService(stocks, coupons, mock(MallTicketDao.class), mock(StoreTicketDao.class), 15);

        org.junit.jupiter.api.Assertions.assertEquals(BigDecimal.ONE, service.preview(reservationCommand()).getTotalDiscount());
        org.junit.jupiter.api.Assertions.assertEquals(CouponStatus.AVAILABLE, coupon.getStatus());
        verify(coupons).findByCouponNo("CPN-1");
        verify(coupons, org.mockito.Mockito.never()).save(coupon);
    }

    @Test
    public void duplicateCouponInOneReservationRequestIsRejectedBeforeDoubleCounting() {
        CouponStockRepository stocks = mock(CouponStockRepository.class);
        UserCouponRepository coupons = mock(UserCouponRepository.class);
        CouponService service = new CouponService(stocks, coupons, mock(MallTicketDao.class), mock(StoreTicketDao.class), 15);
        CouponReservationCommand command = reservationCommand();
        CouponUse duplicate = new CouponUse();
        duplicate.setCouponNo("CPN-1");
        command.setCoupons(Arrays.asList(command.getCoupons().get(0), duplicate));

        try {
            service.reserve(command);
            fail("one coupon must not be counted twice in one order");
        } catch (TradeException expected) {
            org.junit.jupiter.api.Assertions.assertEquals(422, expected.getStatus());
        }
        verify(coupons, org.mockito.Mockito.never()).findByCouponNoForUpdate("CPN-1");
    }

    @Test
    public void merchantSummaryContainsOnlyAggregateCounts() {
        UserCouponRepository coupons = mock(UserCouponRepository.class);
        when(coupons.countByMerchantId(9L)).thenReturn(10L);
        when(coupons.countByMerchantIdAndStatus(9L, CouponStatus.AVAILABLE)).thenReturn(4L);
        CouponService service = new CouponService(mock(CouponStockRepository.class), coupons, mock(MallTicketDao.class), mock(StoreTicketDao.class), 15);
        java.util.Map<String, Object> summary = service.merchantSummary(9L);
        org.junit.jupiter.api.Assertions.assertEquals(10L, summary.get("issued"));
        org.junit.jupiter.api.Assertions.assertEquals(4L, summary.get("available"));
        org.junit.jupiter.api.Assertions.assertFalse(summary.containsKey("couponNo"));
    }

    @Test
    public void claimBeforeReceiveWindowIsRejected() {
        CouponStockRepository stocks = mock(CouponStockRepository.class);
        MallTicketDao tickets = mock(MallTicketDao.class);
        MallTicket ticket = ticket();
        Calendar start = Calendar.getInstance(); start.add(Calendar.DAY_OF_MONTH, 1); ticket.setReceiveStartTime(start.getTime());
        when(tickets.getFromId(7)).thenReturn(ticket);
        CouponService service = new CouponService(stocks, mock(UserCouponRepository.class), tickets, mock(StoreTicketDao.class), 15);
        try { service.claim(1L, claimCommand()); fail("future coupon receive window must reject claim"); }
        catch (TradeException expected) { org.junit.jupiter.api.Assertions.assertEquals(409, expected.getStatus()); }
    }

    @Test
    public void buyerCanClaimActiveMerchantOwnedTemplate() {
        CouponStockRepository stocks = mock(CouponStockRepository.class);
        CouponStock stock = new CouponStock("MERCHANT_TEMPLATE:8", 3);
        when(stocks.findByScopeKeyForUpdate("MERCHANT_TEMPLATE:8")).thenReturn(stock);
        UserCouponRepository coupons = mock(UserCouponRepository.class);
        when(coupons.saveAndFlush(any(UserCoupon.class))).thenAnswer(invocation -> invocation.getArguments()[0]);
        MerchantCouponTemplateRepository templates = mock(MerchantCouponTemplateRepository.class);
        Calendar calendar = Calendar.getInstance(); calendar.add(Calendar.DAY_OF_MONTH, 1);
        MerchantCouponTemplate template = new MerchantCouponTemplate(9L, "store coupon", 3, BigDecimal.ZERO, BigDecimal.ONE, null, null, calendar.getTime());
        org.springframework.test.util.ReflectionTestUtils.setField(template, "id", 8L);
        when(templates.findByIdForUpdate(8L)).thenReturn(template);
        CouponService service = new CouponService(stocks, coupons, mock(MallTicketDao.class), mock(StoreTicketDao.class), templates, 15);
        ClaimCouponCommand command = new ClaimCouponCommand(); command.setCouponType("STORE"); command.setMerchantTemplateId(8L);
        UserCoupon claimed = service.claim(1L, command);
        org.junit.jupiter.api.Assertions.assertEquals(Long.valueOf(9L), claimed.getMerchantId());
        org.junit.jupiter.api.Assertions.assertEquals("MERCHANT_TEMPLATE:8", claimed.getSourceKey());
        verify(stocks).insertIfAbsent("MERCHANT_TEMPLATE:8", 3);
    }

    private ClaimCouponCommand claimCommand() {
        ClaimCouponCommand command = new ClaimCouponCommand();
        command.setCouponType("MALL");
        command.setTicketId(7L);
        return command;
    }

    private MallTicket ticket() {
        MallTicket ticket = new MallTicket();
        ticket.setNumber(1);
        ticket.setFull(0);
        ticket.setMinux(1);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        ticket.setApplyEndTime(calendar.getTime());
        return ticket;
    }

    private CouponReservationCommand reservationCommand() {
        CouponUse use = new CouponUse();
        use.setCouponNo("CPN-1");
        CouponReservationCommand command = new CouponReservationCommand();
        command.setOrderNo("ORD-1");
        command.setBuyerId(1L);
        command.setParentAmount(BigDecimal.TEN);
        command.setMerchantAmounts(Collections.singletonMap(10L, BigDecimal.TEN));
        command.setCoupons(Collections.singletonList(use));
        return command;
    }
}
