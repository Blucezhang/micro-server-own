package com.own.user.party.dao.domain;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity
public class Fun {

	@GraphId
	private Long functionId;
	@Property(name="name")
	private String name;
	@Property(name="note")
	private String note;
	@Property(name="config")
	private Long config;
	@Property(name="dicconfig")
	private Long dicConfig;
	@Property(name="biztypeid")
	private Integer bizTypeId;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getFunctionId() {
		return functionId;
	}

	public void setFunctionId(Long functionId) {
		this.functionId = functionId;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Long getConfig() {
		return config;
	}

	public void setConfig(Long config) {
		this.config = config;
	}

	public Long getDicConfig() {
		return dicConfig;
	}

	public void setDicConfig(Long dicConfig) {
		this.dicConfig = dicConfig;
	}

	public Integer getBizTypeId() {
		return bizTypeId;
	}

	public void setBizTypeId(Integer bizTypeId) {
		this.bizTypeId = bizTypeId;
	}
	

}
