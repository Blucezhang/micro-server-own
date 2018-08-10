package com.own.promotion.dao.domain;

import lombok.Data;

import java.util.List;

@Data
public class FullPromotion extends BasePromotion{

	private Integer basePrice;
	private Integer addPrice;
	private List<String> giftes;
	private boolean isChecked;
}
