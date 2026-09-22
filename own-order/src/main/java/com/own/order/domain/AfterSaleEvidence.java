package com.own.order.domain;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ord_after_sale_evidence")
public class AfterSaleEvidence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "after_sale_no", nullable = false, length = 64) private String afterSaleNo;
    @Column(name = "stored_name", nullable = false, length = 255) private String storedName;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    protected AfterSaleEvidence() { }
    public AfterSaleEvidence(String afterSaleNo, String storedName) {
        this.afterSaleNo = afterSaleNo; this.storedName = storedName; this.createdAt = new Date();
    }
    public Long getId() { return id; }
    public String getAfterSaleNo() { return afterSaleNo; }
    public String getStoredName() { return storedName; }
    public Date getCreatedAt() { return createdAt; }
}
