package com.own.settlement.domain;

import java.math.BigDecimal;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "stl_withdrawal")
public class MerchantWithdrawal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "withdrawal_no", nullable = false, unique = true, length = 64) private String withdrawalNo;
    @Column(name = "merchant_id", nullable = false) private Long merchantId;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 24) private String status;
    @Column(name = "created_at", nullable = false) private Date createdAt;
    @Column(name = "paid_at") private Date paidAt;
    protected MerchantWithdrawal() { }
    public MerchantWithdrawal(String withdrawalNo, Long merchantId, BigDecimal amount) { this.withdrawalNo = withdrawalNo; this.merchantId = merchantId; this.amount = amount; this.status = "REQUESTED"; this.createdAt = new Date(); }
    public void pay() { if (!"REQUESTED".equals(status)) throw new IllegalStateException("withdrawal cannot be paid"); status = "PAID"; paidAt = new Date(); }
    public String getWithdrawalNo() { return withdrawalNo; } public Long getMerchantId() { return merchantId; } public BigDecimal getAmount() { return amount; } public String getStatus() { return status; }
}
