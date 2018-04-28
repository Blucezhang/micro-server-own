package com.own.user.party.dao.domain;

import lombok.Data;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@Data
@NodeEntity
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
 