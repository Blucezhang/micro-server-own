package com.own.promotion.coupon.service;

import com.own.face.trade.TradeException;
import com.own.promotion.coupon.domain.MerchantCouponTemplate;
import com.own.promotion.coupon.dto.MerchantCouponTemplateCommand;
import com.own.promotion.coupon.repository.CouponStockRepository;
import com.own.promotion.coupon.repository.MerchantCouponTemplateRepository;
import java.math.BigDecimal;
import java.util.Date;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MerchantCouponTemplateTimeTest {
    private final MerchantCouponTemplateRepository templates = mock(MerchantCouponTemplateRepository.class);
    private final CouponService service = new CouponService(mock(CouponStockRepository.class), null, null, null, templates, 15);
    private final long expiresAt = System.currentTimeMillis() + 86_400_000L;

    @Test
    void rejectsStartAfterExpiryWithoutClaimEndOnCreate() {
        TradeException error = assertThrows(TradeException.class,
                () -> service.createMerchantTemplate(9L, command(expiresAt + 1000L)));
        assertEquals(422, error.getStatus());
        verify(templates, never()).save(any());
    }

    @Test
    void rejectsStartAfterExpiryWithoutClaimEndOnUpdate() {
        when(templates.findByIdForUpdate(7L)).thenReturn(new MerchantCouponTemplate(9L, "old", 3,
                BigDecimal.ZERO, BigDecimal.ONE, null, null, new Date(expiresAt)));
        TradeException error = assertThrows(TradeException.class,
                () -> service.updateMerchantTemplate(9L, 7L, command(expiresAt + 1000L)));
        assertEquals(422, error.getStatus());
        verify(templates, never()).save(any());
    }

    @Test
    void acceptsValidStartWithoutClaimEnd() {
        when(templates.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        MerchantCouponTemplate template = service.createMerchantTemplate(9L, command(expiresAt - 1000L));
        assertEquals(new Date(expiresAt), template.getExpiresAt());
        assertEquals(new Date(expiresAt - 1000L), template.getClaimStartsAt());
    }

    private MerchantCouponTemplateCommand command(long start) {
        MerchantCouponTemplateCommand command = new MerchantCouponTemplateCommand();
        command.setName("coupon"); command.setTotalQuantity(3);
        command.setMinimumAmount("10"); command.setDiscountAmount("1");
        command.setClaimStartsAt(start); command.setExpiresAt(expiresAt);
        return command;
    }
}
