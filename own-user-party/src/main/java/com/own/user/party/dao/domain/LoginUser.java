package com.own.user.party.dao.domain;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

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
	
	public Long getPartmentId() {
		return partmentId;
	}

	public void setPartmentId(Long partmentId) {
		this.partmentId = partmentId;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public Long getLoginUserId() {
		return loginUserId;
	}

	public void setLoginUserId(Long loginUserId) {
		this.loginUserId = loginUserId;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public Integer getLoginId() {
		return loginId;
	}

	public void setLoginId(Integer loginId) {
		this.loginId = loginId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getPartyId() {
		return partyId;
	}

	public void setPartyId(Long partyId) {
		this.partyId = partyId;
	}

	public String getTrspwd() {
		return trspwd;
	}

	public void setTrspwd(String trspwd) {
		this.trspwd = trspwd;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	

}
