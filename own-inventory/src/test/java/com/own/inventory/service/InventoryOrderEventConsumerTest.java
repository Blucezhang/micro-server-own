package com.own.inventory.service;

import com.own.face.trade.inbox.TradeMessageInboxService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InventoryOrderEventConsumerTest {
    @Test
    void ownedCancellationEventsExecuteLocalReleaseInsideInbox() {
        var inbox = mock(TradeMessageInboxService.class); var service = mock(InventoryService.class);
        when(inbox.consume(anyString(), any(), any(), any())).thenAnswer(call -> { ((Runnable) call.getArgument(3)).run(); return true; });
        var consumer = new InventoryOrderEventConsumer().tradeOrderEventInbox(inbox, service, "inventory-saga");
        for (String type : new String[] {"ORDER_CANCELED", "ORDER_EXPIRED"}) {
            consumer.accept(Map.of("eventId", type, "orderNo", "ORD-1", "eventType", type));
        }
        verify(service, times(2)).releaseOrder("ORD-1");
    }
    @Test
    void nonOwnedEventsAreNotMarkedAsProcessed() {
        var inbox = mock(TradeMessageInboxService.class); var service = mock(InventoryService.class);
        new InventoryOrderEventConsumer().tradeOrderEventInbox(inbox, service, "inventory-saga")
                .accept(Map.of("eventId", 1, "orderNo", "ORD-1", "eventType", "PAYMENT_SUCCEEDED"));
        verifyNoInteractions(inbox, service);
    }
}
