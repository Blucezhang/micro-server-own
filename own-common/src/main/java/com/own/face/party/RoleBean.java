package com.own.face.party;

import lombok.Data;

import java.util.List;

@Data
public class RoleBean {
	
	private String note;
	private String name;
	private Long role;
	private Long orgId; //组织ID
	private Long partmentId;//部门ID
	private List<Long> funIdsList;
}
