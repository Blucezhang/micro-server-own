package com.own.owncommon.product;

import lombok.Data;

import java.util.List;

@Data
public class  TemplateBean {
	
	private Long id;
	private String name;
	private String content;
	private List<Long> productTypeIdList;
}
