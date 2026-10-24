package com.own.promotion.coupon.controller;

import com.own.face.security.InternalServiceGuard;
import com.own.face.trade.TradeException;
import com.own.promotion.coupon.domain.CouponType;
import com.own.promotion.coupon.domain.UserCoupon;
import com.own.promotion.coupon.repository.UserCouponRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CouponRecoveryControllerTest {
    private static final String TOKEN = "isolated-internal-token-at-least-32-bytes";
    private final UserCouponRepository rows = mock(UserCouponRepository.class);
    private final CouponRecoveryController controller = new CouponRecoveryController(rows, new InternalServiceGuard(TOKEN, ""));
    @Test
    void untrustedRequestsCannotReadBuyerCouponBindings() {
        assertEquals(403, assertThrows(TradeException.class, () -> controller.state("ORD-1", request(null, "SYSTEM"))).getStatus());
        assertEquals(403, assertThrows(TradeException.class, () -> controller.state("ORD-1", request(TOKEN, "BUYER"))).getStatus());
        verifyNoInteractions(rows);
    }
    @Test
    void projectsOnlyCurrentOrderBindingsAndAmounts() {
        UserCoupon coupon = new UserCoupon("CPN-1", 7L, CouponType.STORE, 1L, 20L, BigDecimal.TEN, BigDecimal.ONE, new Date());
        coupon.reserve("ORD-1", new Date()); coupon.consume();
        when(rows.findByReservedOrderNoOrderByIdAsc("ORD-1")).thenReturn(List.of(coupon));
        Object data = controller.state("ORD-1", request(TOKEN, "SYSTEM")).getData();
        assertEquals(List.of(Map.of("couponNo", "CPN-1", "buyerId", 7L, "orderNo", "ORD-1", "status", "USED", "discountAmount", BigDecimal.ONE)), data);
        verify(rows).findByReservedOrderNoOrderByIdAsc("ORD-1"); verifyNoMoreInteractions(rows);
    }
    private HttpServletRequest request(String token, String actor) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader(InternalServiceGuard.HEADER)).thenReturn(token);
        when(request.getHeader("X-Actor-Id")).thenReturn("1"); when(request.getHeader("X-Actor-Type")).thenReturn(actor);
        return request;
    }
}
