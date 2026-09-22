package com.own.promotion.dao.domain;

import java.io.Serializable;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node
public class BasePromotion implements Serializable{

	@Id
    @GeneratedValue
	private Long id;
	@Property(name="saleName")
	private String saleName;
	@Property(name="startTime")
	private String startTime;
	@Property(name="endTime")
	private String endTime;
	@Property(name="slogan")
	private String slogan;//广告语
	@Property(name="saleAddress")
	private String saleAddress;
	@Property(name="saleMark")
	private String saleMark;
	@Property(name="createTime")
	private String createTime;
	@Property(name="saleStatus")
	private int saleStatus;//活动状态
	@Property(name="examineStatus")
	private int examineStatus;//审核状态
	@Property(name="userName")
	private String userName;
	@Property(name="userAccount")
	private String userAccount;
	@Property(name="userRole")
	private String userRole;
	@Property(name="flag")
	private int flag;
	@Property(name="allJoin")
	private boolean allJoin;

}
