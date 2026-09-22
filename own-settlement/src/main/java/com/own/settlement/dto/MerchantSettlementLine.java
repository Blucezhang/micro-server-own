package com.own.settlement.dto;
import java.math.BigDecimal;
/** Trusted order projection consumed inside settlement, not an external API payload. */
public class MerchantSettlementLine {
    private Long merchantId; private BigDecimal grossAmount;
    public MerchantSettlementLine() { }
    public MerchantSettlementLine(Long merchantId, BigDecimal grossAmount) { this.merchantId = merchantId; this.grossAmount = grossAmount; }
    public Long getMerchantId() { return merchantId; } public void setMerchantId(Long value) { merchantId = value; }
    public BigDecimal getGrossAmount() { return grossAmount; } public void setGrossAmount(BigDecimal value) { grossAmount = value; }
}
