package com.own.workflow.domain;
 
import lombok.Data;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
 

@Entity
@Table(name="View_ProcConvert")
@Data
public class FlowView implements Serializable,IDomainBase {

	@Id
	@Column(name = "convertId", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long convertId;
 	private Long processId;
 	private Long prodId;
	private Integer bizType;
	private String bizName;
	private String funName;
	private String district;
	private Long originOrg;
	private Integer status;
	private Long partyId;
	private Long functionId;
	private Boolean sysAutoFlag;
	private Integer preStatus;
	private Integer nextStatus;
	@Override
	public Object getObjectId() {
		return processId;
	}

		
		
}
