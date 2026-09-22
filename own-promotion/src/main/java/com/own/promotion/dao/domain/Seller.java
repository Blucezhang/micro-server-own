package com.own.promotion.dao.domain;

import java.util.Date;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Node
public class Seller {
	
	@Id
    @GeneratedValue
	private Long id;
	private String sellerId;
	private String sellerName;
	private Date createTime;

}
