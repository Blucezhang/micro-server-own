package com.own.order.service;

import com.own.order.domain.OrderSaga;
import com.own.order.domain.OrderSagaStatus;
import com.own.order.repository.OrderSagaRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class OrderSagaSchedulerTest {
    @Test
    void failedOrderDoesNotStopLaterProcessingOrCompensation() {
        OrderSagaRepository sagas = Mockito.mock(OrderSagaRepository.class);
        AsyncOrderSagaService service = Mockito.mock(AsyncOrderSagaService.class);
        OrderSaga first = new OrderSaga("SAGA-1", "ORD-1", "{}");
        OrderSaga second = new OrderSaga("SAGA-2", "ORD-2", "{}");
        OrderSaga compensating = new OrderSaga("SAGA-3", "ORD-3", "{}");
        compensating.compensate("retry", com.own.order.domain.OrderSagaStep.COMPENSATE_INVENTORY);
        Mockito.when(sagas.findByStatusAndNextAttemptAtBeforeOrderByIdAsc(
                Mockito.eq(OrderSagaStatus.PROCESSING), Mockito.any(Date.class)))
                .thenReturn(Arrays.asList(first, second));
        Mockito.when(sagas.findByStatusAndNextAttemptAtBeforeOrderByIdAsc(
                Mockito.eq(OrderSagaStatus.COMPENSATING), Mockito.any(Date.class)))
                .thenReturn(Collections.singletonList(compensating));
        Mockito.doThrow(new IllegalStateException("local write failed")).when(service).process("ORD-1");

        new OrderSagaScheduler(sagas, service, true).processPendingSagas();

        Mockito.verify(service).process("ORD-1");
        Mockito.verify(service).process("ORD-2");
        Mockito.verify(service).process("ORD-3");
    }
}
