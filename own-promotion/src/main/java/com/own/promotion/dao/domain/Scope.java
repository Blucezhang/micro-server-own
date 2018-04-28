package com.own.promotion.dao.domain;

import java.util.Date;

import lombok.Data;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@Data
@NodeEntity
public class Scope {

	@GraphId
	private Long id;
	private String name;
	private String scope;
	private Date createTime;
	private int flag;
}
