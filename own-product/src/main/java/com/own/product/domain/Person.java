package com.own.product.domain;

import lombok.Data;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@Data
@NodeEntity
public class Person extends Party{
	
	@Property(name="name")
	private String name;
	@Property(name="age")
	private Integer age;
	@Property(name="orgId")
	private Long orgId;

}