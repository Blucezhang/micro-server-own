package com.own.promotion.dao.domain;

import java.util.Date;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Product {
	@GraphId
	private Long id;
	private String productId;//商品id
	private String productName;//商品名称
	private String productSku;//sku
	private String productType;//商品类型
	private String productSellerId;
	private String price;//原价
	private String promotionPrice;//促销价
	private String stocksNum;
	private String salesNum;
	private String brandId;
	private String partyId;
	
	
	private Date createTime;
	private boolean isJoin;
	private int flag;
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getProductSku() {
		return productSku;
	}
	public void setProductSku(String productSku) {
		this.productSku = productSku;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public boolean isJoin() {
		return isJoin;
	}
	public void setJoin(boolean isJoin) {
		this.isJoin = isJoin;
	}
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public String getProductType() {
		return productType;
	}
	public void setProductType(String productType) {
		this.productType = productType;
	}
	public String getProductSellerId() {
		return productSellerId;
	}
	public void setProductSellerId(String productSellerId) {
		this.productSellerId = productSellerId;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getPromotionPrice() {
		return promotionPrice;
	}
	public void setPromotionPrice(String promotionPrice) {
		this.promotionPrice = promotionPrice;
	}
	public String getStocksNum() {
		return stocksNum;
	}
	public void setStocksNum(String stocksNum) {
		this.stocksNum = stocksNum;
	}
	public String getSalesNum() {
		return salesNum;
	}
	public void setSalesNum(String salesNum) {
		this.salesNum = salesNum;
	}
	public String getBrandId() {
		return brandId;
	}
	public void setBrandId(String brandId) {
		this.brandId = brandId;
	}
	public String getPartyId() {
		return partyId;
	}
	public void setPartyId(String partyId) {
		this.partyId = partyId;
	}
	
	
}
