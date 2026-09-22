package com.own.product.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node
public class Category {

	@Id
    @GeneratedValue
	private Long id;
	@Property(name="name")
	private String name;
	@Property(name="level")
	private String level;
	@Property(name="remark")
	private String remark;

}
