package com.own.user.party.dao.domain;

import java.util.List;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node
public class Person extends Party {

	@Property(name = "name")
	private String name;
	@Property(name = "age")
	private Integer age;
	@Property(name = "orgId")
	private Long orgId;
	@Property(name="email")
	private String email;
	@Property(name="phone")
	private String phone;
	@Property(name="flag")
	private Integer  flag;
	@Property(name="shipaddres")
	private List<String> shipaddres;

}