package com.own.order.service;

import com.own.face.trade.ActorType;
import com.own.face.trade.TradeActor;
import com.own.order.domain.AfterSale;
import com.own.order.domain.AfterSaleStatus;
import com.own.order.domain.AfterSaleType;
import com.own.order.dto.AfterSaleAuditCommand;
import com.own.order.repository.AfterSaleItemRepository;
import com.own.order.repository.AfterSaleRepository;
import com.own.order.repository.OrderEventRepository;
import com.own.order.repository.OrderItemRepository;
import com.own.order.repository.TradeOrderRepository;
import com.own.order.repository.TradeSubOrderRepository;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AfterSaleDispatchTest {
    @Test
    void committedPendingRefundSurvivesDispatchFailureAndSchedulerRetriesIt() {
        AfterSale sale = applyingSale();
        AfterSaleRepository sales = mock(AfterSaleRepository.class);
        SettlementClient settlement = mock(SettlementClient.class);
        when(sales.findByAfterSaleNoForUpdate("AS-1")).thenReturn(sale);
        when(sales.findByStatus(AfterSaleStatus.REFUND_PENDING)).thenReturn(Collections.singletonList(sale));
        doThrow(new IllegalStateException("settlement temporarily unavailable"))
                .doNothing().when(settlement).refundAfterSale(sale);
        RecordingTransactions transactions = new RecordingTransactions();
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(transactions);
        interceptor.setTransactionAttributeSource(new AnnotationTransactionAttributeSource());
        ProxyFactory proxy = new ProxyFactory(service(sales, settlement));
        proxy.addAdvice(interceptor);
        AfterSaleService transactional = (AfterSaleService) proxy.getProxy();

        AfterSale result = assertDoesNotThrow(() -> transactional.audit(
                new TradeActor(2L, ActorType.MERCHANT), "AS-1", approval()));
        assertEquals(AfterSaleStatus.REFUND_PENDING, result.getStatus());
        assertEquals(1, transactions.commits);
        assertEquals(0, transactions.rollbacks);

        new AfterSaleRefundRetryScheduler(sales, settlement).retryPendingRefunds();
        verify(settlement, times(2)).refundAfterSale(sale);
        verify(sales).save(sale);
    }

    @Test
    void rolledBackApprovalDoesNotDispatchRefund() {
        AfterSale sale = applyingSale();
        AfterSaleRepository sales = mock(AfterSaleRepository.class);
        SettlementClient settlement = mock(SettlementClient.class);
        when(sales.findByAfterSaleNoForUpdate("AS-1")).thenReturn(sale);
        beginSynchronization();
        try {
            service(sales, settlement).audit(new TradeActor(2L, ActorType.MERCHANT), "AS-1", approval());
            TransactionSynchronizationUtils.triggerAfterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            verifyNoInteractions(settlement);
        } finally {
            endSynchronization();
        }
    }

    @Test
    void refundDispatchRequiresAnActualTransactionWithSynchronization() {
        AfterSale sale = applyingSale();
        AfterSaleRepository sales = mock(AfterSaleRepository.class);
        SettlementClient settlement = mock(SettlementClient.class);
        when(sales.findByAfterSaleNoForUpdate("AS-1")).thenReturn(sale);

        assertThrows(IllegalStateException.class, () -> service(sales, settlement)
                .audit(new TradeActor(2L, ActorType.MERCHANT), "AS-1", approval()));
        verifyNoInteractions(settlement);
    }

    private AfterSale applyingSale() {
        return new AfterSale("AS-1", "ORD-1", "SUB-1", 1L, 2L,
                AfterSaleType.REFUND_ONLY, BigDecimal.ONE, "missing item");
    }

    private AfterSaleAuditCommand approval() {
        AfterSaleAuditCommand command = new AfterSaleAuditCommand();
        command.setApproved(true);
        return command;
    }

    private AfterSaleService service(AfterSaleRepository sales, SettlementClient settlement) {
        AfterSaleService service = new AfterSaleService(sales, mock(AfterSaleItemRepository.class),
                mock(TradeSubOrderRepository.class), mock(TradeOrderRepository.class),
                mock(OrderItemRepository.class), mock(OrderEventRepository.class));
        ReflectionTestUtils.setField(service, "settlement", settlement);
        return service;
    }

    private void beginSynchronization() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionSynchronizationManager.setActualTransactionActive(true);
    }

    private void endSynchronization() {
        TransactionSynchronizationManager.setActualTransactionActive(false);
        TransactionSynchronizationManager.clearSynchronization();
    }

    private static class RecordingTransactions extends AbstractPlatformTransactionManager {
        private int commits;
        private int rollbacks;
        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) { }
        @Override protected void doCommit(DefaultTransactionStatus status) { commits++; }
        @Override protected void doRollback(DefaultTransactionStatus status) { rollbacks++; }
    }
}
