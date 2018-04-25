package com.own.user.party.dao.domain;

import java.util.List;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity
public class Person extends Party {

	@Property(name = "name")
	private String name;

	@Property(name = "age")
	private Integer age;

	@Property(name = "orgId")
	private Long orgId;

	@Property(name="email")
	private String email;

	@Property(name="phone")
	private String phone;
	
	@Property(name="flag")
	private Integer  flag;
	
	@Property(name="shipaddres")
	private List<String> shipaddres;
	
	
	public List<String> getShipaddres() {
		return shipaddres;
	}

	public void setShipaddres(List<String> shipaddres) {
		this.shipaddres = shipaddres;
	}

	public Integer getFlag() {
		return flag;
	}

	public void setFlag(Integer flag) {
		this.flag = flag;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

}