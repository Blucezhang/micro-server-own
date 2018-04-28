package com.own.promotion.dao.domain;

import java.util.Date;

import lombok.Data;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@Data
@NodeEntity
public class PromotionType {
	
	@GraphId
	private Long id;
	private  String saleTypeName;
	private String saleTypeAlias;
	private Date createTime;
	private String userName;
	private String userAccount;
	private int flag;
	private String saleTypeMark;
	
	

}
