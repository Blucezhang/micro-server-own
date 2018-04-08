package com.own.face.party;

public class LoginUserBean {
	
	private String loginUserName;
	
	private Integer loginId;
	
	private Long partyid;
	
	private String password;
	
	private String name;
	
	private String email;
	
	private String phone;
	
	private Long partmentId;
	
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

	public String getLoginUserName() {
		return loginUserName;
	}

	public void setLoginUserName(String loginUserName) {
		this.loginUserName = loginUserName;
	}
	
	public String getPassword() {
		return password;
	}

	public Integer getLoginId() {
		return loginId;
	}

	public void setLoginId(Integer loginId) {
		this.loginId = loginId;
	}

	public Long getPartyid() {
		return partyid;
	}

	public void setPartyid(Long partyid) {
		this.partyid = partyid;
	}

	public void setPassword(String password) {
		this.password = password;
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
