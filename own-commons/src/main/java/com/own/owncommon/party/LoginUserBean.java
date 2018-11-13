package com.own.owncommon.party;

import lombok.Data;

@Data
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

}
