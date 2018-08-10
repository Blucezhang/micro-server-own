package com.own.promotion.dao.domain;

import java.util.Date;

import lombok.Data;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@Data
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
	

}
