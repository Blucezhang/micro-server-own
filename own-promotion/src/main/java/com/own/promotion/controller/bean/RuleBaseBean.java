package com.own.promotion.controller.bean;

import lombok.Data;

import java.util.Date;

@Data
public class RuleBaseBean {
	
	private Long id;
	
	private String saleName;

	private Date startTime;
	
	private Date endTime;
	
	private String slogan;//广告语
	
	private String saleAddress;
	
	private String saleMark;
	
	private Date createTime;
	
	private int saleStatus;//活动状态
	
	private int examineStatus;//审核状态
	
	private String userName;
	
	private String userAccount;
	
	private String userRole;
	
	private int flag;
	
	private boolean allJoin;//是否全场参加
	
	private int promotionTypeId;//关联促销类型的id
	
	private int scopeId;//关联促销范围的id
}
