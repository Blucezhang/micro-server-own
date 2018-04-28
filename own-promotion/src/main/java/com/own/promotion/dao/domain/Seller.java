package com.own.promotion.dao.domain;

import java.util.Date;

import lombok.Data;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@Data
@NodeEntity
public class Seller {
	
	@GraphId
	private Long id;
	private String sellerId;
	private String sellerName;
	private Date createTime;

}
