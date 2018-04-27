package com.own.workflow.domain;
 
import lombok.Data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
 

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
