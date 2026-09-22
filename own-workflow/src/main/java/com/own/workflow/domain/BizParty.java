package com.own.workflow.domain;

import lombok.Data;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
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

}
