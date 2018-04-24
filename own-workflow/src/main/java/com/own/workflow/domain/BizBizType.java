package com.own.workflow.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="Biz_BizType")
public class BizBizType implements Serializable {

	private static final long serialVersionUID = 322594978128004935L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private String bizTypesId;
	private Long processId;
	private Long bizTypeId;
	private String note;
	private Boolean currFlag;

	@Temporal(TemporalType.TIMESTAMP)
	private Date updateTime;

	public Long getProcessId() {
		return processId;
	}
	public void setProcessId(Long processId) {
		this.processId = processId;
	}
	public Long getBizTypeId() {
		return bizTypeId;
	}
	public void setBizTypeId(Long bizTypeId) {
		this.bizTypeId = bizTypeId;
	}

	public String getBizTypesId() {
		return bizTypesId;
	}
	public void setBizTypesId(String bizTypesId) {
		this.bizTypesId = bizTypesId;
	}
 	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public Boolean getCurrFlag() {
		return currFlag;
	}
	public void setCurrFlag(Boolean currFlag) {
		this.currFlag = currFlag;
	}
 

	}
