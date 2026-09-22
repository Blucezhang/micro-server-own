package com.own.promotion.dao.domain;

import java.util.Date;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Node
public class Scope {

	@Id
    @GeneratedValue
	private Long id;
	private String name;
	private String scope;
	private Date createTime;
	private int flag;
}
