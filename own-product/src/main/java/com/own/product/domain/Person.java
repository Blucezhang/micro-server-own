package com.own.product.domain;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node
public class Person extends Party{
	
	@Property(name="name")
	private String name;
	@Property(name="age")
	private Integer age;
	@Property(name="orgId")
	private Long orgId;

}