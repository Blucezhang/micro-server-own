package com.own.face.product;

import lombok.Data;

import java.util.List;


@Data
public class ProductBean {

	private Long id;
	private String name;
	private String originalPrice;
	private String promotionPrice;
	private String stocksNum;
	private String salesNum;
	private Long categoryId;
	private Long brandId;
	private Long partyId;
	private String content;
	private List<Long> productTypeIdList;

}
