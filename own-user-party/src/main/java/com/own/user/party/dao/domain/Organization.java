package com.own.user.party.dao.domain;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node
public class Organization extends Party{

	@Property(name="orgId")
	private Long orgId;
	@Property(name="name")
	private String name;
	@Property(name="level")
	private Integer level;
	@Property(name="email")
	private String email;
	@Property(name="phone")
	private String phone;
	@Property(name="loginname")
	private String loginName;


}
 