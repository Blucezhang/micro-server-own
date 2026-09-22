package com.own.promotion.dao.domain;

import java.util.Date;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Node
public class PromotionType {
	
	@Id
    @GeneratedValue
	private Long id;
	private  String saleTypeName;
	private String saleTypeAlias;
	private Date createTime;
	private String userName;
	private String userAccount;
	private int flag;
	private String saleTypeMark;
	
	

}
