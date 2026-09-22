package com.own.order.dto;

import com.own.order.domain.AfterSale;
import com.own.order.domain.AfterSaleStatus;
import com.own.order.domain.AfterSaleType;
import java.math.BigDecimal;
import java.util.Date;

/** Participant-safe after-sale projection; persistent buyer and merchant IDs stay internal. */
public class AfterSaleView {
    private final String afterSaleNo;
    private final String orderNo;
    private final String subOrderNo;
    private final AfterSaleType type;
    private final AfterSaleStatus status;
    private final BigDecimal requestedAmount;
    private final String reason;
    private final String returnCompany;
    private final String returnTrackingNo;
    private final String merchantRemark;
    private final Date lastReminderAt;

    public AfterSaleView(AfterSale sale) {
        this.afterSaleNo = sale.getAfterSaleNo(); this.orderNo = sale.getOrderNo(); this.subOrderNo = sale.getSubOrderNo();
        this.type = sale.getType(); this.status = sale.getStatus(); this.requestedAmount = sale.getRequestedAmount();
        this.reason = sale.getReason(); this.returnCompany = sale.getReturnCompany(); this.returnTrackingNo = sale.getReturnTrackingNo();
        this.merchantRemark = sale.getMerchantRemark(); this.lastReminderAt = sale.getLastReminderAt();
    }
    public String getAfterSaleNo() { return afterSaleNo; } public String getOrderNo() { return orderNo; }
    public String getSubOrderNo() { return subOrderNo; } public AfterSaleType getType() { return type; }
    public AfterSaleStatus getStatus() { return status; } public BigDecimal getRequestedAmount() { return requestedAmount; }
    public String getReason() { return reason; } public String getReturnCompany() { return returnCompany; }
    public String getReturnTrackingNo() { return returnTrackingNo; } public String getMerchantRemark() { return merchantRemark; }
    public Date getLastReminderAt() { return lastReminderAt; }
}
