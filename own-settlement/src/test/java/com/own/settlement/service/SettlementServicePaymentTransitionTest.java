package com.own.settlement.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.settlement.domain.Payment;
import com.own.settlement.domain.PaymentStatus;
import com.own.settlement.domain.PaymentChannel;
import com.own.settlement.dto.MockPaymentCallbackCommand;
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
    public void paymentDetailRequiresBuyerRoleEvenWhenMerchantIdMatchesBuyerId() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        Mockito.when(payments.findByPaymentNo("PAY-1")).thenReturn(payment);
        SettlementService service = new SettlementService(payments, Mockito.mock(OrderClient.class),
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
        SettlementService service = new SettlementService(payments, orders, Mockito.mock(RefundRepository.class),
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
        SettlementService service = new SettlementService(payments, orders, Mockito.mock(RefundRepository.class),
                Mockito.mock(AfterSaleRefundRepository.class));

        Payment result = service.simulateSuccess(new TradeActor(1L, ActorType.SYSTEM), "PAY-1");

        Assertions.assertSame(payment, result);
        Mockito.verify(orders, Mockito.never()).paymentSucceeded(Mockito.anyString());
        Mockito.verify(payments, Mockito.never()).save(Mockito.any(Payment.class));
    }

    @Test
    public void paymentSuccessWritesMerchantReceivableFromProtectedOrderProjection() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class); OrderClient orders = Mockito.mock(OrderClient.class);
        MerchantSettlementService ledger = Mockito.mock(MerchantSettlementService.class);
        Mockito.when(payments.findByPaymentNoForUpdate("PAY-1")).thenReturn(payment); Mockito.when(payments.save(payment)).thenReturn(payment);
        java.util.List<MerchantSettlementLine> lines = Arrays.asList(new MerchantSettlementLine(2L, java.math.BigDecimal.ONE));
        Mockito.when(orders.settlementLines("ORD-1")).thenReturn(lines);
        SettlementService service = new SettlementService(payments, orders, Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));
        ReflectionTestUtils.setField(service, "merchantSettlementService", ledger);
        service.simulateSuccess(new TradeActor(1L, ActorType.SYSTEM), "PAY-1");
        Mockito.verify(ledger).recordPaidOrder("ORD-1", lines);
    }

    @Test
    public void mockChannelCallbackVerifiesReferenceAndAmountBeforeSuccess() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, new java.math.BigDecimal("12.50"), "key-1", PaymentChannel.MOCK_ALIPAY);
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        OrderClient orders = Mockito.mock(OrderClient.class);
        Mockito.when(payments.findByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
        Mockito.when(payments.save(payment)).thenReturn(payment);
        SettlementService service = new SettlementService(payments, orders, Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));
        MockPaymentCallbackCommand callback = new MockPaymentCallbackCommand();
        callback.setPaymentNo("PAY-1"); callback.setProviderPaymentNo(payment.getProviderPaymentNo());
        callback.setAmount(new java.math.BigDecimal("12.50")); callback.setResult("SUCCESS");

        Payment result = service.processMockCallback(PaymentChannel.MOCK_ALIPAY, callback);

        Assertions.assertEquals(PaymentStatus.SUCCEEDED, result.getStatus());
        Mockito.verify(orders).paymentSucceeded("ORD-1");
    }

    @Test
    public void mockChannelCallbackRejectsWrongProviderReference() {
        Payment payment = new Payment("PAY-1", "ORD-1", 1L, java.math.BigDecimal.ONE, "key-1");
        PaymentRepository payments = Mockito.mock(PaymentRepository.class);
        Mockito.when(payments.findByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
        SettlementService service = new SettlementService(payments, Mockito.mock(OrderClient.class), Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));
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
        SettlementService service = new SettlementService(payments, orders, Mockito.mock(RefundRepository.class), Mockito.mock(AfterSaleRefundRepository.class));
        ReflectionTestUtils.setField(service, "merchantSettlementService", ledger);

        service.refund(new TradeActor(1L, ActorType.BUYER), "ORD-1");

        InOrder sequence = Mockito.inOrder(orders, ledger);
        sequence.verify(orders).refundSucceeded("ORD-1");
        sequence.verify(ledger).reverseOrder("ORD-1");
    }
}
