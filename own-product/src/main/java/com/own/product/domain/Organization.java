package com.own.product.domain;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node
public class Organization extends Party{

	@Property(name="name")
	private String name;
	@Property(name="level")
	private Integer level;


}
 