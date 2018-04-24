package com.own.promotion.controller.bean;

import lombok.Data;

import java.util.Date;

@Data
public class SellerBean {
	
	private Long id;
	
	private String sellerId;
	
	private String sellerName;
	
	private Date createTime;
	
	private int promotionId;

}
