package com.own.owncommon.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BuyerBean {

	private Long buyerId;
	private Long orderDetailId;
	private Long partyId;
	private String productName;
	private Integer productQuantity;
	private BigDecimal productPrice;

}
