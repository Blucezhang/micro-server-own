package com.own.workflow.domain;


import lombok.Data;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
