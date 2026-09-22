package com.own.user.party.dao.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node
public class Fun {

	@Id
    @GeneratedValue
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
