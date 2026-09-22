package com.own.workflow.domain;


import lombok.Data;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name="Biz_Relation")
public class BizRelation implements Serializable {


	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private String bizRelationId;
	private Long firstProcessId;
	private Long secondProcessId;
	private Integer bizRelationTypeId;
}
