package com.own.workflow.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

@Data
@Entity
@Table(name="Biz_State")
public class BizState implements Serializable, IDomainBase {

	@Transient
	public Object getObjectId() {
		return this.stateId;
	}

	@Id
	@Column(name="stateId", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long stateId;
	@Temporal(TemporalType.TIMESTAMP)
	private Date stateTime;
	private Long processId;
	private Integer exeNum;
	private Integer agreeNum;
	private Integer strategyType;
	private Integer desentNum;
	private Integer status;
	private String stateDesc;
	private Boolean currFlag;
	private Boolean runningFlag;
	private Boolean finishFlag;
	private Boolean stateType;
}
