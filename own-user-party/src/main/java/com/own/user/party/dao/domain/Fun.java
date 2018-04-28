package com.own.user.party.dao.domain;

import lombok.Data;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@Data
@NodeEntity
public class Fun {

	@GraphId
	private Long functionId;
	@Property(name="name")
	private String name;
	@Property(name="note")
	private String note;
	@Property(name="config")
	private Long config;
	@Property(name="dicconfig")
	private Long dicConfig;
	@Property(name="biztypeid")
	private Integer bizTypeId;

}
