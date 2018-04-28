package com.own.product.domain;

import lombok.Data;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@Data
@NodeEntity
public class Template {
	@GraphId
	private Long id;
	@Property(name="name")
	private String name;
	@Property(name="content")
	private String content;

}
