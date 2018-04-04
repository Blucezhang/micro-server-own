package com.own.face.party;

import java.util.List;

public class RoleBean {
	
	private String note;
	private String name;
	private Long role;
	private Long orgId; //组织ID
	private Long partmentId;//部门ID
	private List<Long> funIdsList;
	
	public Long getOrgId() {
		return orgId;
	}
	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
	public Long getPartmentId() {
		return partmentId;
	}
	public void setPartmentId(Long partmentId) {
		this.partmentId = partmentId;
	}
	public List<Long> getFunIdsList() {
		return funIdsList;
	}
	public void setFunIdsList(List<Long> funIdsList) {
		this.funIdsList = funIdsList;
	}
	public Long getRole() {
		return role;
	}
	public void setRole(Long role) {
		this.role = role;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	

}
