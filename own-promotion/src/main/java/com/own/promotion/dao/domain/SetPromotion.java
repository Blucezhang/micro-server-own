package com.own.promotion.dao.domain;

import lombok.Data;

@Data
public class SetPromotion extends BasePromotion{

	private Integer singleBuyCount;
	private boolean oneLimit;
}
