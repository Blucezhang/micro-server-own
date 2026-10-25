package com.own.settlement.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.face.trade.TradeException;
import com.own.settlement.domain.AfterSaleRefund;
import com.own.settlement.domain.MerchantReceivable;
import com.own.settlement.domain.Payment;
import com.own.settlement.domain.PaymentStatus;
import com.own.settlement.domain.Refund;
import com.own.settlement.dto.AfterSaleRefundCommand;
import com.own.settlement.repository.AfterSaleRefundRepository;
import com.own.settlement.repository.PaymentRepository;
import com.own.settlement.repository.RefundRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SettlementDownstreamFailureTest {
    private final PaymentRepository payments = mock(PaymentRepository.class);
    private final RefundRepository refunds = mock(RefundRepository.class);
    private final AfterSaleRefundRepository afterSales = mock(AfterSaleRefundRepository.class);
    private final MerchantSettlementService merchants = mock(MerchantSettlementService.class);
    private final RestTemplate rest = mock(RestTemplate.class);

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void rejectedOrderPaymentCallbackDoesNotCreateReceivableOrSucceedPayment() {
        Payment payment = payment();
        when(payments.findByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
        when(rest.exchange(endsWith("/settlement-lines"), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("status", 200, "data", List.of(Map.of("merchantId", 2, "grossAmount", "10.00")))));
        rejectCallbacks();

        assertThrows(TradeException.class, () -> service().simulateSuccess(new TradeActor(1L, ActorType.SYSTEM), "PAY-1"));

        assertEquals(PaymentStatus.CREATED, payment.getStatus());
        verify(merchants, never()).recordPaidOrder(anyString(), anyList());
        verify(payments, never()).save(any(Payment.class));
    }

    @Test
    void rejectedOrderRefundCallbackDoesNotReverseReceivableOrCompletePaymentRefund() {
        Payment payment = payment(); payment.succeed();
        when(payments.findByOrderNoForUpdate("ORD-1")).thenReturn(payment);
        rejectCallbacks();

        assertThrows(TradeException.class, () -> service().refund(new TradeActor(7L, ActorType.BUYER), "ORD-1"));

        assertEquals(PaymentStatus.SUCCEEDED, payment.getStatus());
        verify(merchants, never()).reverseOrder(anyString());
        verify(refunds, never()).save(any(Refund.class));
        verify(payments, never()).save(any(Payment.class));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void failedAfterSaleAcknowledgementSurfacesAndExplicitRetryDoesNotAllocateTwice() {
        Payment payment = payment(); payment.succeed();
        when(payments.findByOrderNoForUpdate("ORD-1")).thenReturn(payment);
        when(afterSales.save(any(AfterSaleRefund.class))).thenAnswer(call -> call.getArgument(0));
        doAnswer(call -> {
            AfterSaleRefund refund = call.getArgument(0);
            refund.allocate(new MerchantReceivable("ORD-1", 2L, BigDecimal.TEN, new BigDecimal("0.80")), BigDecimal.ZERO, BigDecimal.ZERO);
            return null;
        }).when(merchants).allocateAfterSaleRefund(any(AfterSaleRefund.class));
        rejectCallbacks();
        AfterSaleRefundCommand command = command();
        SettlementService service = service();
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
        try {
            AfterSaleRefund existing = service.refundAfterSale(new TradeActor(1L, ActorType.SYSTEM), command);
            assertEquals("APPLIED", existing.getReconciliationStatus());
            TradeException error = assertThrows(TradeException.class, TransactionSynchronizationUtils::triggerAfterCommit);
            assertEquals(409, error.getStatus());
            when(afterSales.findByAfterSaleNo("AS-1")).thenReturn(existing);
            when(rest.exchange(endsWith("/refund-succeeded"), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                    .thenReturn(ResponseEntity.ok(Map.of("status", 200, "data", "AS-1")));

            assertSame(existing, service.refundAfterSale(new TradeActor(1L, ActorType.SYSTEM), command));

            verify(afterSales, times(1)).save(any(AfterSaleRefund.class));
            verify(merchants, times(1)).allocateAfterSaleRefund(any(AfterSaleRefund.class));
        } finally {
            TransactionSynchronizationManager.setActualTransactionActive(false);
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private SettlementService service() {
        SettlementService service = new SettlementService(payments, new OrderClient(rest, "token", "http://own-order"), refunds, afterSales);
        ReflectionTestUtils.setField(service, "merchantSettlementService", merchants);
        SettlementRecoveryJournal journal = mock(SettlementRecoveryJournal.class);
        when(journal.begin(any(Payment.class), any(com.own.settlement.domain.RecoveryAction.class))).thenReturn(1);
        ReflectionTestUtils.setField(service, "recoveryJournal", journal);
        return service;
    }

    private Payment payment() { return new Payment("PAY-1", "ORD-1", 7L, BigDecimal.TEN, "key"); }

    private AfterSaleRefundCommand command() {
        AfterSaleRefundCommand command = new AfterSaleRefundCommand();
        command.setAfterSaleNo("AS-1"); command.setOrderNo("ORD-1"); command.setBuyerId(7L);
        command.setMerchantId(2L); command.setAmount(BigDecimal.ONE);
        return command;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void rejectCallbacks() {
        when(rest.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("status", 409, "data", "looks-successful")));
    }
}
