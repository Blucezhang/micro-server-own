package com.own.product.domain;

import lombok.Data;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@Data
@NodeEntity
public class Organization extends Party{

	@Property(name="name")
	private String name;
	@Property(name="level")
	private Integer level;


}
 