package com.own.owncommon.product;


import lombok.Data;

@Data
public class CategoryBean {

	private Long id;
	private String name;
	private String level;
	private String remark;
	private Long pId;
}
