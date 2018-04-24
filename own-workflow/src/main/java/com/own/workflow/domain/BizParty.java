package com.own.workflow.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
 
@Entity
@Table(name="Biz_Party")
public class BizParty implements Serializable {

	private static final long serialVersionUID = -9129050135050885452L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long bizPartyId;
 
	private Long partyId;

 
	private Long processId;

 
	private Integer roleType;

	public Long getBizPartyId() {
		return bizPartyId;
	}

	public void setBizPartyId(Long bizPartyId) {
		this.bizPartyId = bizPartyId;
	}
 

	}
