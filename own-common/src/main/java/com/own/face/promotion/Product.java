package com.own.face.promotion;

import lombok.Data;

import java.util.Date;

@Data
public class Product {
	private Long id;
	private String productId;
	private String productName;
	private String productSku;
	private Date createTime;
	private boolean isJoin;
	private int flag;
}
