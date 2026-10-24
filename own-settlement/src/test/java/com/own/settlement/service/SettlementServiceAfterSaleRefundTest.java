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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

public class SettlementServiceAfterSaleRefundTest {
    @Test
    public void waitingRetryReadsRefundLedgerOnlyAfterAcquiringPaymentLock() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, new BigDecimal("10.00"), "payment-key");
        payment.succeed();
        AfterSaleRefund committed = new AfterSaleRefund("ASR-1", "AS-1", payment, 2L, new BigDecimal("4.00"));
        java.util.concurrent.atomic.AtomicReference<AfterSaleRefund> visible = new java.util.concurrent.atomic.AtomicReference<>();
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        AfterSaleRefundRepository refunds = Mockito.mock(AfterSaleRefundRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        Mockito.when(refunds.findByAfterSaleNo("AS-1")).thenAnswer(invocation -> visible.get());
        Mockito.when(payments.findByOrderNoForUpdate("ORD-1")).thenAnswer(invocation -> {
            visible.set(committed); // The earlier request commits while this request waits for the payment lock.
            return payment;
        });
        SettlementService service = new SettlementService(payments, orders, Mockito.mock(RefundRepository.class), refunds);

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            AfterSaleRefund result = service.refundAfterSale(new TradeActor(1L, ActorType.SYSTEM), command());

            Assertions.assertSame(committed, result);
            Mockito.verify(refunds, Mockito.never()).save(Mockito.any(AfterSaleRefund.class));
            org.mockito.InOrder sequence = Mockito.inOrder(payments, refunds);
            sequence.verify(payments).findByOrderNoForUpdate("ORD-1");
            sequence.verify(refunds).findByAfterSaleNo("AS-1");
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    public void retryForSameAfterSaleReusesLedgerRecord() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, new BigDecimal("10.00"), "payment-key");
        payment.succeed();
        AfterSaleRefund existing = new AfterSaleRefund("ASR-1", "AS-1", payment, 2L, new BigDecimal("4.00"));
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        AfterSaleRefundRepository refunds = Mockito.mock(AfterSaleRefundRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        Mockito.when(refunds.findByAfterSaleNo("AS-1")).thenReturn(existing);
        Mockito.when(payments.findByOrderNoForUpdate("ORD-1")).thenReturn(payment);
        SettlementService service = new SettlementService(payments, orders, Mockito.mock(RefundRepository.class), refunds);

        AfterSaleRefund result = withinTransaction(() -> service.refundAfterSale(new TradeActor(1L, ActorType.SYSTEM), command()));

        Assertions.assertSame(existing, result);
        Mockito.verify(refunds, Mockito.never()).save(Mockito.any(AfterSaleRefund.class));
        Mockito.verify(orders).afterSaleRefundSucceeded("AS-1");
    }

    @Test
    public void refundReplayMustStillBelongToTheOriginalPaymentBuyer() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, new BigDecimal("10.00"), "payment-key");
        payment.succeed();
        AfterSaleRefund existing = new AfterSaleRefund("ASR-1", "AS-1", payment, 2L, new BigDecimal("4.00"));
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        AfterSaleRefundRepository refunds = Mockito.mock(AfterSaleRefundRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        Mockito.when(refunds.findByAfterSaleNo("AS-1")).thenReturn(existing);
        Mockito.when(payments.findByOrderNoForUpdate("ORD-1")).thenReturn(payment);
        SettlementService service = new SettlementService(payments, orders, Mockito.mock(RefundRepository.class), refunds);
        AfterSaleRefundCommand command = command();
        command.setBuyerId(2L);

        TradeException error = Assertions.assertThrows(TradeException.class,
                () -> withinTransaction(() -> service.refundAfterSale(new TradeActor(1L, ActorType.SYSTEM), command)));

        Assertions.assertEquals(404, error.getStatus());
        Mockito.verify(orders, Mockito.never()).afterSaleRefundSucceeded(Mockito.anyString());
        Mockito.verify(refunds, Mockito.never()).save(Mockito.any(AfterSaleRefund.class));
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

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            Assertions.assertThrows(TradeException.class,
                    () -> service.refundAfterSale(new TradeActor(1L, ActorType.SYSTEM), command()));
            Mockito.verify(refunds, Mockito.never()).save(Mockito.any(AfterSaleRefund.class));
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    public void newRefundNotifiesOrderOnlyAfterLedgerCommit() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, new BigDecimal("10.00"), "payment-key");
        payment.succeed();
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        AfterSaleRefundRepository refunds = Mockito.mock(AfterSaleRefundRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        Mockito.when(payments.findByOrderNoForUpdate("ORD-1")).thenReturn(payment);
        Mockito.when(refunds.save(Mockito.any(AfterSaleRefund.class))).thenAnswer(invocation -> invocation.getArgument(0));
        SettlementService service = new SettlementService(payments, orders, Mockito.mock(RefundRepository.class), refunds);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "merchantSettlementService", Mockito.mock(MerchantSettlementService.class));

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            AfterSaleRefund result = service.refundAfterSale(new TradeActor(1L, ActorType.SYSTEM), command());
            Assertions.assertNotNull(result);
            Mockito.verify(refunds).save(Mockito.any(AfterSaleRefund.class));
            Mockito.verify(orders, Mockito.never()).afterSaleRefundSucceeded("AS-1");
            TransactionSynchronizationUtils.triggerAfterCommit();
            Mockito.verify(orders).afterSaleRefundSucceeded("AS-1");
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    public void rolledBackNewRefundDoesNotNotifyOrder() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, new BigDecimal("10.00"), "payment-key");
        payment.succeed();
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        AfterSaleRefundRepository refunds = Mockito.mock(AfterSaleRefundRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        Mockito.when(payments.findByOrderNoForUpdate("ORD-1")).thenReturn(payment);
        SettlementService service = new SettlementService(payments, orders, Mockito.mock(RefundRepository.class), refunds);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "merchantSettlementService", Mockito.mock(MerchantSettlementService.class));

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            service.refundAfterSale(new TradeActor(1L, ActorType.SYSTEM), command());
            TransactionSynchronizationUtils.triggerAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            Mockito.verify(orders, Mockito.never()).afterSaleRefundSucceeded("AS-1");
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    public void newRefundFailsClosedWithoutTransactionSynchronization() {
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        AfterSaleRefundRepository refunds = Mockito.mock(AfterSaleRefundRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        SettlementService service = new SettlementService(payments, orders, Mockito.mock(RefundRepository.class), refunds);

        Assertions.assertThrows(IllegalStateException.class,
                () -> service.refundAfterSale(new TradeActor(1L, ActorType.SYSTEM), command()));

        Mockito.verify(payments, Mockito.never()).findByOrderNoForUpdate(Mockito.anyString());
        Mockito.verify(refunds, Mockito.never()).save(Mockito.any(AfterSaleRefund.class));
        Mockito.verify(orders, Mockito.never()).afterSaleRefundSucceeded(Mockito.anyString());
    }

    @Test
    public void afterSaleRefundRejectsFractionsOfACentBeforeLedgerWrite() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, new BigDecimal("10.00"), "payment-key");
        payment.succeed();
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        AfterSaleRefundRepository refunds = Mockito.mock(AfterSaleRefundRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        Mockito.when(payments.findByOrderNoForUpdate("ORD-1")).thenReturn(payment);
        SettlementService service = new SettlementService(payments, orders, Mockito.mock(RefundRepository.class), refunds);
        AfterSaleRefundCommand command = command();
        command.setAmount(new BigDecimal("4.001"));

        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            TradeException error = Assertions.assertThrows(TradeException.class,
                    () -> service.refundAfterSale(new TradeActor(1L, ActorType.SYSTEM), command));
            Assertions.assertEquals(422, error.getStatus());
            Mockito.verify(refunds, Mockito.never()).save(Mockito.any(AfterSaleRefund.class));
            Mockito.verify(orders, Mockito.never()).afterSaleRefundSucceeded(Mockito.anyString());
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private <T> T withinTransaction(java.util.function.Supplier<T> action) {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            return action.get();
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private AfterSaleRefundCommand command() {
        AfterSaleRefundCommand command = new AfterSaleRefundCommand();
        command.setAfterSaleNo("AS-1"); command.setOrderNo("ORD-1"); command.setBuyerId(1L); command.setMerchantId(2L); command.setAmount(new BigDecimal("4.00"));
        return command;
    }
}
