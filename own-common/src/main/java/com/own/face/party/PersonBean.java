package com.own.face.party;

import lombok.Data;

import java.util.List;

@Data
public class PersonBean {

	private Integer age;
	private String email;
	private String phone;
	private Long partmentId;
	private Long orgId;
	private String name;
	private String loginUserName;
	private String password;
	private Long personid;
	private List<String> shipaddress;
	private Integer flag ; //判断是买家还是卖家  0是买家，1是卖家

}
