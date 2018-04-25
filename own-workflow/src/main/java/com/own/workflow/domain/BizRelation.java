package com.own.workflow.domain;


import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="Biz_Relation")
public class BizRelation implements Serializable {

	private static final long serialVersionUID = -3921977620097393758L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private String bizRelationId;

 
	private Long firstProcessId;

 
	private Long secondProcessId;

 
	private Integer bizRelationTypeId;

	public String getBizRelationId() {
		return bizRelationId;
	}

	public void setBizRelationId(String bizRelationId) {
		this.bizRelationId = bizRelationId;
	}
  
	}
