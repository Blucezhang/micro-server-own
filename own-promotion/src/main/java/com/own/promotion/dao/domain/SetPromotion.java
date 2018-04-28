package com.own.promotion.dao.domain;

import lombok.Data;

@Data
public class SetPromotion extends BasePromotion{

	private static final long serialVersionUID = 1L;
	
	private Integer singleBuyCount;
	private boolean oneLimit;

}
