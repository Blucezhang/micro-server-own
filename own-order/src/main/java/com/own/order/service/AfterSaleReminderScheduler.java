package com.own.order.service;

import com.own.order.domain.AfterSale;
import com.own.order.domain.AfterSaleStatus;
import com.own.order.domain.OrderEvent;
import com.own.order.repository.AfterSaleRepository;
import com.own.order.repository.OrderEventRepository;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AfterSaleReminderScheduler {
    private final AfterSaleRepository sales;
    private final OrderEventRepository events;
    private final int auditHours;
    private final int repeatHours;

    public AfterSaleReminderScheduler(AfterSaleRepository sales, OrderEventRepository events,
            @Value("${trade.after-sale.audit-reminder-hours:48}") int auditHours,
            @Value("${trade.after-sale.reminder-repeat-hours:24}") int repeatHours) {
        this.sales = sales;
        this.events = events;
        this.auditHours = auditHours;
        this.repeatHours = repeatHours;
    }

    @Scheduled(fixedDelayString = "${trade.after-sale.reminder-delay-millis:3600000}")
    @Transactional
    public void remindOverdueAudits() {
        if (auditHours <= 0) {
            return;
        }
        Date now = new Date();
        Date deadline = new Date(now.getTime() - auditHours * 3600L * 1000L);
        for (AfterSale sale : sales.findByStatusAndCreatedAtBefore(AfterSaleStatus.APPLYING, deadline)) {
            Date repeatDeadline = new Date(now.getTime() - repeatHours * 3600L * 1000L);
            if (sale.getLastReminderAt() != null && sale.getLastReminderAt().after(repeatDeadline)) {
                continue;
            }
            sale.remind();
            sales.save(sale);
            events.save(new OrderEvent(sale.getOrderNo(), sale.getSubOrderNo(),
                    "AFTER_SALE_AUDIT_REMINDER", 0L, "SYSTEM", "merchant_audit_overdue"));
        }
    }
}
