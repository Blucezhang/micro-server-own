package com.own.inventory.dto;

/** Merchant command; merchant ownership always comes from the actor header. */
public class LowStockAlertRuleCommand {
    private Integer thresholdQuantity;
    private Boolean enabled;

    public Integer getThresholdQuantity() { return thresholdQuantity; }
    public void setThresholdQuantity(Integer thresholdQuantity) { this.thresholdQuantity = thresholdQuantity; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
