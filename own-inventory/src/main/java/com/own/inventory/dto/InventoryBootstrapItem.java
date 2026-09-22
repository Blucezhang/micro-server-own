package com.own.inventory.dto;

public class InventoryBootstrapItem {
    private Long productId;
    private Long merchantId;
    private String stocksNum;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }
    public String getStocksNum() { return stocksNum; }
    public void setStocksNum(String stocksNum) { this.stocksNum = stocksNum; }
}
