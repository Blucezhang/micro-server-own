package com.own.settlement.service;

import com.own.face.trade.inbox.OrderEventTypes;
import com.own.face.trade.TradeException;
import com.own.face.trade.inbox.TradeMessageInboxService;
import com.own.face.trade.inbox.TradeOrderEventMessage;
import com.own.settlement.domain.Payment;
import com.own.settlement.domain.RecoveryAction;
import com.own.settlement.repository.PaymentRepository;
import java.util.Map;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettlementOrderEventConsumer {
    @Bean
    public Consumer<Map<String, Object>> tradeOrderEventInbox(TradeMessageInboxService inbox, PaymentRepository payments,
            SettlementService service, @Value("${trade.inbox.consumer-name}") String consumerName) {
        return event -> {
            TradeOrderEventMessage message = TradeOrderEventMessage.read(event);
            RecoveryAction action;
            if (OrderEventTypes.PAYMENT_SUCCEEDED.equals(message.eventType())) action = RecoveryAction.PAYMENT;
            else if (OrderEventTypes.REFUND_SUCCEEDED.equals(message.eventType())) action = RecoveryAction.REFUND;
            else return;
            inbox.consume(consumerName, message.eventId(), event, () -> {
                Payment payment = payments.findByOrderNo(message.orderNo());
                if (payment == null) throw TradeException.conflict("order event has no matching payment");
                // Reuse the existing fact-checked coordinator; a message never creates a payment or a second ledger.
                service.recover(payment.getPaymentNo(), action);
            });
        };
    }
}
