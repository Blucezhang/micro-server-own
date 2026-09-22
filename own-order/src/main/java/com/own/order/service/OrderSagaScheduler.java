package com.own.order.service;

import com.own.order.domain.OrderSaga;
import com.own.order.domain.OrderSagaStatus;
import com.own.order.repository.OrderSagaRepository;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderSagaScheduler {
    private final OrderSagaRepository sagas;
    private final AsyncOrderSagaService service;
    private final boolean enabled;

    public OrderSagaScheduler(OrderSagaRepository sagas, AsyncOrderSagaService service,
                              @Value("${trade.saga.async-enabled:true}") boolean enabled) {
        this.sagas = sagas; this.service = service; this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${trade.saga.delay-millis:1000}")
    public void processPendingSagas() {
        if (!enabled) return;
        for (OrderSaga saga : sagas.findByStatusAndNextAttemptAtBeforeOrderByIdAsc(OrderSagaStatus.PROCESSING, new Date())) {
            service.process(saga.getOrderNo());
        }
        for (OrderSaga saga : sagas.findByStatusAndNextAttemptAtBeforeOrderByIdAsc(OrderSagaStatus.COMPENSATING, new Date())) {
            service.process(saga.getOrderNo());
        }
    }
}
