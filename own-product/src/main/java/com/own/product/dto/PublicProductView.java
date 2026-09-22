package com.own.product.dto;

import com.own.product.domain.Product;

/** Storefront projection: deliberately omits internal initial-stock and sales counters. */
public class PublicProductView {
    private final Long id;
    private final String name;
    private final String originalPrice;
    private final String promotionPrice;
    private final Long categoryId;
    private final Long brandId;
    private final Long partyId;
    private final String content;
    private final String skuCode;
    private final String specification;
    private final String saleStatus;

    public PublicProductView(Product product) {
        this.id = product.getId(); this.name = product.getName(); this.originalPrice = product.getOriginalPrice();
        this.promotionPrice = product.getPromotionPrice(); this.categoryId = product.getCategoryId(); this.brandId = product.getBrandId();
        this.partyId = product.getPartyId(); this.content = product.getContent(); this.skuCode = product.getSkuCode();
        this.specification = product.getSpecification(); this.saleStatus = product.getSaleStatus() == null ? "AVAILABLE" : product.getSaleStatus();
    }

    public Long getId() { return id; } public String getName() { return name; } public String getOriginalPrice() { return originalPrice; }
    public String getPromotionPrice() { return promotionPrice; } public Long getCategoryId() { return categoryId; } public Long getBrandId() { return brandId; }
    public Long getPartyId() { return partyId; } public String getContent() { return content; } public String getSkuCode() { return skuCode; }
    public String getSpecification() { return specification; } public String getSaleStatus() { return saleStatus; }
}
