package com.own.user.party.dao.domain;

import lombok.Data;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@Data
@NodeEntity
public class LoginUser {
	
	@GraphId
	private Long loginUserId;
	@Property(name="loginName")
	private String loginName;
	@Property(name="loginId")
	private Integer loginId;
	@Property(name="password")
	private String password;
	@Property(name="partyId")
	private Long partyId;
	@Property(name="trspwd")
	private String trspwd;
	@Property(name="name")
	private String name;
	@Property(name="email")
	private String email;
	@Property(name="phone")
	private String phone;
	@Property(name="partmentid")
	private Long partmentId;
	@Property(name="orgid")
	private Long orgId;

}
