package com.own.settlement.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.settlement.domain.AfterSaleRefund;
import com.own.settlement.domain.Payment;
import com.own.settlement.dto.AfterSaleRefundCommand;
import com.own.settlement.repository.AfterSaleRefundRepository;
import com.own.settlement.repository.PaymentRepository;
import com.own.settlement.repository.RefundRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SettlementServiceAfterSaleRefundTest {
    @Test
    public void retryForSameAfterSaleReusesLedgerRecord() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, new BigDecimal("10.00"), "payment-key");
        payment.succeed();
        AfterSaleRefund existing = new AfterSaleRefund("ASR-1", "AS-1", payment, new BigDecimal("4.00"));
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        AfterSaleRefundRepository refunds = Mockito.mock(AfterSaleRefundRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        Mockito.when(refunds.findByAfterSaleNo("AS-1")).thenReturn(existing);
        SettlementService service = new SettlementService(payments, orders, Mockito.mock(RefundRepository.class), refunds);

        AfterSaleRefund result = service.refundAfterSale(new TradeActor(1L, ActorType.SYSTEM), command());

        Assertions.assertSame(existing, result);
        Mockito.verify(refunds, Mockito.never()).save(Mockito.any(AfterSaleRefund.class));
        Mockito.verify(orders).afterSaleRefundSucceeded("AS-1");
    }

    @Test
    public void refundsCannotExceedTheOriginalPaymentAmount() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, new BigDecimal("10.00"), "payment-key");
        payment.succeed();
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        AfterSaleRefundRepository refunds = Mockito.mock(AfterSaleRefundRepository.class);
        Mockito.when(payments.findByOrderNoForUpdate("ORD-1")).thenReturn(payment);
        Mockito.when(refunds.succeededAmountByPaymentNo("PAY-1")).thenReturn(new BigDecimal("8.00"));
        SettlementService service = new SettlementService(payments, Mockito.mock(OrderClient.class),
                Mockito.mock(RefundRepository.class), refunds);

        try {
            service.refundAfterSale(new TradeActor(1L, ActorType.SYSTEM), command());
            Assertions.fail("refunds above the paid amount must be rejected");
        } catch (TradeException expected) {
            Mockito.verify(refunds, Mockito.never()).save(Mockito.any(AfterSaleRefund.class));
        }
    }

    private AfterSaleRefundCommand command() {
        AfterSaleRefundCommand command = new AfterSaleRefundCommand();
        command.setAfterSaleNo("AS-1"); command.setOrderNo("ORD-1"); command.setBuyerId(1L); command.setAmount(new BigDecimal("4.00"));
        return command;
    }
}
