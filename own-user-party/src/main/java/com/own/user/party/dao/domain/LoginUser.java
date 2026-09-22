package com.own.user.party.dao.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node
public class LoginUser {
	
	@Id
    @GeneratedValue
	private Long loginUserId;
	@Property(name="loginName")
	private String loginName;
	@Property(name="loginId")
	private Integer loginId;
	@Property(name="password")
	@JsonIgnore
	private String password;
	@Property(name="partyId")
	private Long partyId;
	@Property(name="trspwd")
	@JsonIgnore
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
