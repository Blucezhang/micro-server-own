package com.own.promotion.coupon.service;

import com.own.face.trade.inbox.TradeMessageInboxService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CouponOrderEventConsumerTest {
    @Test
    void cancellationUsesExistingCouponReleaseInsideInbox() {
        var inbox = mock(TradeMessageInboxService.class); var service = mock(CouponService.class);
        when(inbox.consume(anyString(), any(), any(), any())).thenAnswer(call -> { ((Runnable) call.getArgument(3)).run(); return true; });
        var consumer = new CouponOrderEventConsumer().tradeOrderEventInbox(inbox, service, "promotion-saga");
        consumer.accept(Map.of("eventId", 1, "orderNo", "ORD-1", "eventType", "ORDER_CANCELED"));
        verify(service).releaseOrder("ORD-1");
    }
    @Test
    void unrelatedEventsDoNotConsumeOrReleaseCoupons() {
        var inbox = mock(TradeMessageInboxService.class); var service = mock(CouponService.class);
        new CouponOrderEventConsumer().tradeOrderEventInbox(inbox, service, "promotion-saga")
                .accept(Map.of("eventId", 1, "orderNo", "ORD-1", "eventType", "REFUND_SUCCEEDED"));
        verifyNoInteractions(inbox, service);
    }
}
