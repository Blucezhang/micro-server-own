package com.own.workflow.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name="Biz_State")
@Data
public class BizState implements Serializable, IDomainBase {

	private static final long serialVersionUID = 3239791447897868700L;

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
