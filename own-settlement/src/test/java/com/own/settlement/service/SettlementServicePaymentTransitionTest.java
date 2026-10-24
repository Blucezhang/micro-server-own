package com.own.settlement.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.settlement.domain.Payment;
import com.own.settlement.domain.PaymentStatus;
import com.own.settlement.domain.PaymentChannel;
import com.own.settlement.dto.MockPaymentCallbackCommand;
import com.own.settlement.dto.CreatePaymentCommand;
import com.own.settlement.repository.AfterSaleRefundRepository;
import com.own.settlement.repository.PaymentRepository;
import com.own.settlement.repository.RefundRepository;
import com.own.settlement.dto.MerchantSettlementLine;
import java.util.Arrays;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.InOrder;

public class SettlementServicePaymentTransitionTest {
    @Test
    public void reusedPaymentKeyCannotReturnAnotherOrderPayment() {
        Payment existing = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        Mockito.when(payments.findByRequestKey("key-1")).thenReturn(existing);
        SettlementService service = settlementService(payments, Mockito.mock(OrderClient.class),
                Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));

        com.own.face.trade.TradeException error = Assertions.assertThrows(com.own.face.trade.TradeException.class,
                () -> service.createPayment(new TradeActor(1L, ActorType.BUYER), paymentCommand("ORD-2", null), "key-1"));

        Assertions.assertEquals(409, error.getStatus());
    }

    @Test
    public void anotherBuyerCannotReceiveExistingPaymentForOrder() {
        Payment existing = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        Mockito.when(payments.findByOrderNo("ORD-1")).thenReturn(existing);
        SettlementService service = settlementService(payments, Mockito.mock(OrderClient.class),
                Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));

        com.own.face.trade.TradeException error = Assertions.assertThrows(com.own.face.trade.TradeException.class,
                () -> service.createPayment(new TradeActor(2L, ActorType.BUYER), paymentCommand("ORD-1", null), "key-2"));

        Assertions.assertEquals(404, error.getStatus());
    }

    @Test
    public void existingPaymentCannotSilentlyChangeChannel() {
        Payment existing = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        Mockito.when(payments.findByOrderNo("ORD-1")).thenReturn(existing);
        SettlementService service = settlementService(payments, Mockito.mock(OrderClient.class),
                Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));

        com.own.face.trade.TradeException error = Assertions.assertThrows(com.own.face.trade.TradeException.class,
                () -> service.createPayment(new TradeActor(1L, ActorType.BUYER),
                        paymentCommand("ORD-1", "MOCK_ALIPAY"), "key-2"));

        Assertions.assertEquals(409, error.getStatus());
    }

    @Test
    public void samePaymentRequestKeyStillReplaysExistingPayment() {
        Payment existing = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        Mockito.when(payments.findByRequestKey("key-1")).thenReturn(existing);
        OrderClient orders = Mockito.mock(OrderClient.class);
        SettlementService service = settlementService(payments, orders,
                Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));

        Assertions.assertSame(existing, service.createPayment(new TradeActor(1L, ActorType.BUYER),
                paymentCommand("ORD-1", null), "key-1"));
        Mockito.verify(orders, Mockito.never()).payableAmount(Mockito.anyString(), Mockito.anyLong());
        Mockito.verify(payments, Mockito.never()).save(Mockito.any(Payment.class));
    }

    @Test
    public void sameBuyerAndChannelCanRetrieveExistingOrderPayment() {
        Payment existing = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        Mockito.when(payments.findByOrderNo("ORD-1")).thenReturn(existing);
        OrderClient orders = Mockito.mock(OrderClient.class);
        SettlementService service = settlementService(payments, orders,
                Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));

        Assertions.assertSame(existing, service.createPayment(new TradeActor(1L, ActorType.BUYER),
                paymentCommand("ORD-1", null), "key-2"));
        Mockito.verify(orders, Mockito.never()).payableAmount(Mockito.anyString(), Mockito.anyLong());
        Mockito.verify(payments, Mockito.never()).save(Mockito.any(Payment.class));
    }

    @Test
    public void newPaymentUsesValidatedOrderAmountAndChannel() {
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        Mockito.when(orders.payableAmount("ORD-1", 1L)).thenReturn(new java.math.BigDecimal("12.50"));
        Mockito.when(payments.save(Mockito.any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        SettlementService service = settlementService(payments, orders,
                Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));

        Payment result = service.createPayment(new TradeActor(1L, ActorType.BUYER),
                paymentCommand("ORD-1", "MOCK_ALIPAY"), "key-1");

        Assertions.assertEquals("ORD-1", result.getOrderNo());
        Assertions.assertEquals(new java.math.BigDecimal("12.50"), result.getAmount());
        Assertions.assertEquals(PaymentChannel.MOCK_ALIPAY, result.getPaymentChannel());
        Assertions.assertEquals(PaymentStatus.CREATED, result.getStatus());
    }

    private CreatePaymentCommand paymentCommand(String orderNo, String channel) {
        CreatePaymentCommand command = new CreatePaymentCommand();
        command.setOrderNo(orderNo);
        command.setChannel(channel);
        return command;
    }

    @Test
    public void paymentDetailRequiresBuyerRoleEvenWhenMerchantIdMatchesBuyerId() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        Mockito.when(payments.findByPaymentNo("PAY-1")).thenReturn(payment);
        SettlementService service = settlementService(payments, Mockito.mock(OrderClient.class),
                Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));

        Assertions.assertSame(payment, service.find(new TradeActor(1L, ActorType.BUYER), "PAY-1"));
        Assertions.assertThrows(com.own.face.trade.TradeException.class,
                () -> service.find(new TradeActor(1L, ActorType.MERCHANT), "PAY-1"));
    }

    @Test
    public void paymentSuccessLocksPaymentBeforeCallingOrderService() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        Mockito.when(payments.findByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
        Mockito.when(payments.save(payment)).thenReturn(payment);
        SettlementService service = settlementService(payments, orders, Mockito.mock(RefundRepository.class),
                Mockito.mock(AfterSaleRefundRepository.class));

        Payment result = service.simulateSuccess(new TradeActor(1L, ActorType.SYSTEM), "PAY-1");

        Assertions.assertSame(payment, result);
        Assertions.assertEquals(PaymentStatus.SUCCEEDED, result.getStatus());
        Mockito.verify(payments).findByPaymentNoForUpdate("PAY-1");
        Mockito.verify(payments, Mockito.never()).findByPaymentNo("PAY-1");
        Mockito.verify(orders).paymentSucceeded("ORD-1");
    }

    @Test
    public void retryOfSuccessfulCallbackDoesNotCallOrderServiceAgain() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        payment.succeed();
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        Mockito.when(payments.findByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
        SettlementService service = settlementService(payments, orders, Mockito.mock(RefundRepository.class),
                Mockito.mock(AfterSaleRefundRepository.class));

        Payment result = service.simulateSuccess(new TradeActor(1L, ActorType.SYSTEM), "PAY-1");

        Assertions.assertSame(payment, result);
        Mockito.verify(orders, Mockito.never()).paymentSucceeded(Mockito.anyString());
        Mockito.verify(payments, Mockito.never()).save(Mockito.any(Payment.class));
    }

    @Test
    public void paymentSuccessWritesMerchantReceivableFromProtectedOrderProjection() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.TEN, "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class); OrderClient orders = Mockito.mock(OrderClient.class);
        MerchantSettlementService ledger = Mockito.mock(MerchantSettlementService.class);
        Mockito.when(payments.findByPaymentNoForUpdate("PAY-1")).thenReturn(payment); Mockito.when(payments.save(payment)).thenReturn(payment);
        java.util.List<MerchantSettlementLine> lines = Arrays.asList(
                new MerchantSettlementLine(2L, new java.math.BigDecimal("4.00")),
                new MerchantSettlementLine(3L, new java.math.BigDecimal("6.00")));
        Mockito.when(orders.settlementLines("ORD-1")).thenReturn(lines);
        SettlementService service = settlementService(payments, orders, Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));
        ReflectionTestUtils.setField(service, "merchantSettlementService", ledger);
        service.simulateSuccess(new TradeActor(1L, ActorType.SYSTEM), "PAY-1");
        Mockito.verify(ledger).recordPaidOrder("ORD-1", lines);
        InOrder sequence = Mockito.inOrder(orders, ledger);
        sequence.verify(orders).settlementLines("ORD-1");
        sequence.verify(orders).paymentSucceeded("ORD-1");
        sequence.verify(ledger).recordPaidOrder("ORD-1", lines);
    }

    @Test
    public void invalidMerchantProjectionCannotAdvancePaymentOrCreditReceivables() {
        java.util.List<java.util.List<MerchantSettlementLine>> invalid = Arrays.asList(
                Arrays.asList(new MerchantSettlementLine(2L, new java.math.BigDecimal("9.00"))),
                Arrays.asList(new MerchantSettlementLine(2L, new java.math.BigDecimal("11.00"))),
                java.util.Collections.emptyList(),
                Arrays.asList(new MerchantSettlementLine(2L, new java.math.BigDecimal("5.00")),
                        new MerchantSettlementLine(2L, new java.math.BigDecimal("5.00"))),
                Arrays.asList(new MerchantSettlementLine(2L, new java.math.BigDecimal("-1.00")),
                        new MerchantSettlementLine(3L, new java.math.BigDecimal("11.00"))),
                Arrays.asList(new MerchantSettlementLine(2L, new java.math.BigDecimal("0.001")),
                        new MerchantSettlementLine(3L, new java.math.BigDecimal("9.999"))),
                java.util.Collections.singletonList(null));
        for (java.util.List<MerchantSettlementLine> lines : invalid) {
            Payment payment = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.TEN, "key-1");
            PaymentRepository payments = Mockito.mock(PaymentRepository.class);
            OrderClient orders = Mockito.mock(OrderClient.class);
            MerchantSettlementService ledger = Mockito.mock(MerchantSettlementService.class);
            Mockito.when(payments.findByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
            Mockito.when(orders.settlementLines("ORD-1")).thenReturn(lines);
            SettlementService service = settlementService(payments, orders,
                    Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));
            ReflectionTestUtils.setField(service, "merchantSettlementService", ledger);

            com.own.face.trade.TradeException error = Assertions.assertThrows(com.own.face.trade.TradeException.class,
                    () -> service.simulateSuccess(new TradeActor(1L, ActorType.SYSTEM), "PAY-1"));

            Assertions.assertEquals(409, error.getStatus());
            Assertions.assertEquals(PaymentStatus.CREATED, payment.getStatus());
            Mockito.verify(orders, Mockito.never()).paymentSucceeded(Mockito.anyString());
            Mockito.verifyNoInteractions(ledger);
            Mockito.verify(payments, Mockito.never()).save(Mockito.any(Payment.class));
        }
    }

    @Test
    public void paymentSuccessCanBeRetriedAfterReceivableWriteFails() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        MerchantSettlementService ledger = Mockito.mock(MerchantSettlementService.class);
        java.util.List<MerchantSettlementLine> lines = Arrays.asList(new MerchantSettlementLine(2L, java.math.BigDecimal.ONE));
        Mockito.when(payments.findByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
        Mockito.when(payments.save(payment)).thenReturn(payment);
        Mockito.when(orders.settlementLines("ORD-1")).thenReturn(lines);
        Mockito.when(ledger.recordPaidOrder("ORD-1", lines))
                .thenThrow(new IllegalStateException("receivable write failed"))
                .thenReturn(java.util.Collections.emptyList());
        SettlementService service = settlementService(payments, orders, Mockito.mock(RefundRepository.class),
                Mockito.mock(AfterSaleRefundRepository.class));
        ReflectionTestUtils.setField(service, "merchantSettlementService", ledger);

        Assertions.assertThrows(IllegalStateException.class,
                () -> service.simulateSuccess(new TradeActor(1L, ActorType.SYSTEM), "PAY-1"));
        Assertions.assertEquals(PaymentStatus.CREATED, payment.getStatus());
        Mockito.verify(payments, Mockito.never()).save(payment);

        Payment result = service.simulateSuccess(new TradeActor(1L, ActorType.SYSTEM), "PAY-1");
        Assertions.assertEquals(PaymentStatus.SUCCEEDED, result.getStatus());
        Mockito.verify(orders, Mockito.times(2)).paymentSucceeded("ORD-1");
        Mockito.verify(ledger, Mockito.times(2)).recordPaidOrder("ORD-1", lines);
        Mockito.verify(payments).save(payment);
    }

    @Test
    public void mockChannelCallbackVerifiesReferenceAndAmountBeforeSuccess() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, new java.math.BigDecimal("12.50"), "key-1", PaymentChannel.MOCK_ALIPAY);
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        Mockito.when(payments.findByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
        Mockito.when(payments.save(payment)).thenReturn(payment);
        SettlementService service = settlementService(payments, orders, Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));
        MockPaymentCallbackCommand callback = new MockPaymentCallbackCommand();
        callback.setPaymentNo("PAY-1"); callback.setProviderPaymentNo(payment.getProviderPaymentNo());
        callback.setAmount(new java.math.BigDecimal("12.50")); callback.setResult("SUCCESS");

        Payment result = service.processMockCallback(PaymentChannel.MOCK_ALIPAY, callback);

        Assertions.assertEquals(PaymentStatus.SUCCEEDED, result.getStatus());
        Payment replay = service.processMockCallback(PaymentChannel.MOCK_ALIPAY, callback);
        Assertions.assertEquals(PaymentStatus.SUCCEEDED, replay.getStatus());
        Mockito.verify(orders).paymentSucceeded("ORD-1");
    }

    @Test
    public void mockChannelCallbackRejectsWrongAmountWithoutChangingPayment() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, new java.math.BigDecimal("12.50"), "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        Mockito.when(payments.findByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
        SettlementService service = settlementService(payments, orders, Mockito.mock(RefundRepository.class),
                Mockito.mock(AfterSaleRefundRepository.class));
        MockPaymentCallbackCommand callback = new MockPaymentCallbackCommand();
        callback.setPaymentNo("PAY-1"); callback.setProviderPaymentNo(payment.getProviderPaymentNo());
        callback.setAmount(new java.math.BigDecimal("12.49")); callback.setResult("SUCCESS");

        com.own.face.trade.TradeException error = Assertions.assertThrows(com.own.face.trade.TradeException.class,
                () -> service.processMockCallback(PaymentChannel.MOCK_WECHAT, callback));

        Assertions.assertEquals(409, error.getStatus());
        Assertions.assertEquals(PaymentStatus.CREATED, payment.getStatus());
        Mockito.verify(orders, Mockito.never()).paymentSucceeded(Mockito.anyString());
        Mockito.verify(payments, Mockito.never()).save(Mockito.any(Payment.class));
    }

    @Test
    public void mockChannelCallbackRejectsWrongProviderReference() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        Mockito.when(payments.findByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
        SettlementService service = settlementService(payments, Mockito.mock(OrderClient.class), Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));
        MockPaymentCallbackCommand callback = new MockPaymentCallbackCommand();
        callback.setPaymentNo("PAY-1"); callback.setProviderPaymentNo("forged"); callback.setAmount(java.math.BigDecimal.ONE); callback.setResult("SUCCESS");
        try {
            service.processMockCallback(PaymentChannel.MOCK_WECHAT, callback);
            Assertions.fail("forged provider reference must be rejected");
        } catch (com.own.face.trade.TradeException expected) {
            Assertions.assertEquals(403, expected.getStatus());
        }
    }

    @Test
    public void fullRefundAdvancesOrderBeforeReversingMerchantReceivable() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        payment.succeed();
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        MerchantSettlementService ledger = Mockito.mock(MerchantSettlementService.class);
        Mockito.when(payments.findByOrderNoForUpdate("ORD-1")).thenReturn(payment);
        Mockito.when(payments.save(payment)).thenReturn(payment);
        SettlementService service = settlementService(payments, orders, Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));
        ReflectionTestUtils.setField(service, "merchantSettlementService", ledger);

        service.refund(new TradeActor(1L, ActorType.BUYER), "ORD-1");

        InOrder sequence = Mockito.inOrder(orders, ledger);
        sequence.verify(orders).refundSucceeded("ORD-1");
        sequence.verify(ledger).reverseOrder("ORD-1");
    }

    @Test
    public void fullRefundCanBeRetriedAfterReceivableReversalFails() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        payment.succeed();
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        RefundRepository refunds = Mockito.mock(RefundRepository.class);
        MerchantSettlementService ledger = Mockito.mock(MerchantSettlementService.class);
        Mockito.when(payments.findByOrderNoForUpdate("ORD-1")).thenReturn(payment);
        Mockito.when(payments.save(payment)).thenReturn(payment);
        Mockito.doThrow(new IllegalStateException("receivable reversal failed"))
                .doNothing().when(ledger).reverseOrder("ORD-1");
        SettlementService service = settlementService(payments, orders, refunds,
                Mockito.mock(AfterSaleRefundRepository.class));
        ReflectionTestUtils.setField(service, "merchantSettlementService", ledger);

        Assertions.assertThrows(IllegalStateException.class,
                () -> service.refund(new TradeActor(1L, ActorType.BUYER), "ORD-1"));
        Assertions.assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        Mockito.verify(refunds, Mockito.never()).save(Mockito.any(com.own.settlement.domain.Refund.class));
        Mockito.verify(payments, Mockito.never()).save(payment);

        Payment result = service.refund(new TradeActor(1L, ActorType.BUYER), "ORD-1");
        Assertions.assertEquals(PaymentStatus.REFUNDED, result.getStatus());
        Mockito.verify(orders, Mockito.times(2)).refundSucceeded("ORD-1");
        Mockito.verify(ledger, Mockito.times(2)).reverseOrder("ORD-1");
        Mockito.verify(refunds).save(Mockito.any(com.own.settlement.domain.Refund.class));
        Mockito.verify(payments).save(payment);
    }

    private SettlementService settlementService(PaymentRepository payments, OrderClient orders,
            RefundRepository refunds, AfterSaleRefundRepository afterSales) {
        SettlementService service = new SettlementService(payments, orders, refunds, afterSales);
        SettlementRecoveryJournal journal = Mockito.mock(SettlementRecoveryJournal.class);
        Mockito.when(journal.begin(Mockito.any(Payment.class), Mockito.any(com.own.settlement.domain.RecoveryAction.class))).thenReturn(1);
        ReflectionTestUtils.setField(service, "recoveryJournal", journal);
        return service;
    }
}
