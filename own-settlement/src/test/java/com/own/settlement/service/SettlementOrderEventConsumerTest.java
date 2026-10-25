package com.own.settlement.service;

import com.own.face.trade.TradeException;
import com.own.face.trade.inbox.TradeMessageInboxService;
import com.own.settlement.domain.Payment;
import com.own.settlement.domain.RecoveryAction;
import com.own.settlement.repository.PaymentRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SettlementOrderEventConsumerTest {
    @Test
    void successfulEventsUseExistingCoordinatorNotANewPaymentOrLedger() {
        var inbox = inbox(); var payments = mock(PaymentRepository.class); var service = mock(SettlementService.class);
        Payment payment = mock(Payment.class); when(payment.getPaymentNo()).thenReturn("PAY-1");
        when(payments.findByOrderNo("ORD-1")).thenReturn(payment);
        var consumer = new SettlementOrderEventConsumer().tradeOrderEventInbox(inbox, payments, service, "settlement-saga");
        consumer.accept(event("PAYMENT_SUCCEEDED")); consumer.accept(event("REFUND_SUCCEEDED"));
        verify(service).recover("PAY-1", RecoveryAction.PAYMENT);
        verify(service).recover("PAY-1", RecoveryAction.REFUND);
        verify(payments, never()).save(any());
    }
    @Test
    void missingPaymentRemainsAnErrorInsteadOfFabricatingSuccess() {
        var payments = mock(PaymentRepository.class); var service = mock(SettlementService.class);
        var consumer = new SettlementOrderEventConsumer().tradeOrderEventInbox(inbox(), payments, service, "settlement-saga");
        assertThrows(TradeException.class, () -> consumer.accept(event("PAYMENT_SUCCEEDED")));
        verifyNoInteractions(service);
    }
    @Test
    void nonOwnedEventsDoNotCreateInboxMarkers() {
        var inbox = inbox(); var payments = mock(PaymentRepository.class); var service = mock(SettlementService.class);
        new SettlementOrderEventConsumer().tradeOrderEventInbox(inbox, payments, service, "settlement-saga").accept(event("ORDER_CANCELED"));
        verifyNoInteractions(inbox, payments, service);
    }
    private Map<String, Object> event(String type) { return Map.of("eventId", type, "orderNo", "ORD-1", "eventType", type); }
    private TradeMessageInboxService inbox() {
        var inbox = mock(TradeMessageInboxService.class);
        when(inbox.consume(anyString(), any(), any(), any())).thenAnswer(call -> { ((Runnable) call.getArgument(3)).run(); return true; });
        return inbox;
    }
}
