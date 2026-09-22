package com.own.settlement.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "stl_batch")
public class SettlementBatch {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "batch_no", nullable = false, unique = true, length = 64) private String batchNo;
    @Column(name = "period_start", nullable = false) private Date periodStart;
    @Column(name = "period_end", nullable = false) private Date periodEnd;
    @Column(nullable = false, length = 24) private String status;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    protected SettlementBatch() { }
    public SettlementBatch(String batchNo, Date periodStart, Date periodEnd) { this.batchNo = batchNo; this.periodStart = periodStart; this.periodEnd = periodEnd; this.status = "SETTLED"; this.createdAt = new Date(); }
    public String getBatchNo() { return batchNo; } public Date getPeriodStart() { return periodStart; } public Date getPeriodEnd() { return periodEnd; } public String getStatus() { return status; }
}
