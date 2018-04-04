package com.own.face.promotion;

import lombok.Data;

import java.util.Date;

@Data
public class Seller {

	private Long id;
	private String sellerId;
	private String sellerName;
	private Date createTime;
	private int promotionId;
	
}
