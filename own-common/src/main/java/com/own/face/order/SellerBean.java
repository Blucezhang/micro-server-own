package com.own.face.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SellerBean {
	
	private Long sellerId;
	private Long orderDetailId;
	private Long partyId;
	private String productName;
	private Integer productQuantity;
	private BigDecimal productPrice;
}
