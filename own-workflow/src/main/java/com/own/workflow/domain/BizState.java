package com.own.workflow.domain;

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

	public Long getStateId() {
		return stateId;
	}
	public void setStateId(Long stateId) {
		this.stateId = stateId;
	}
	public Date getStateTime() {
		return stateTime;
	}
	public void setStateTime(Date stateTime) {
		this.stateTime = stateTime;
	}
	public Long getProcessId() {
		return processId;
	}
	public void setProcessId(Long processId) {
		this.processId = processId;
	}
	public Integer getExeNum() {
		return exeNum;
	}
	public void setExeNum(Integer exeNum) {
		this.exeNum = exeNum;
	}
	public Integer getAgreeNum() {
		return agreeNum;
	}
	public void setAgreeNum(Integer agreeNum) {
		this.agreeNum = agreeNum;
	}
	public Integer getStrategyType() {
		return strategyType;
	}
	public void setStrategyType(Integer strategyType) {
		this.strategyType = strategyType;
	}
	public Integer getDesentNum() {
		return desentNum;
	}
	public void setDesentNum(Integer desentNum) {
		this.desentNum = desentNum;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getStateDesc() {
		return stateDesc;
	}
	public void setStateDesc(String stateDesc) {
		this.stateDesc = stateDesc;
	}
	public Boolean getCurrFlag() {
		return currFlag;
	}
	public void setCurrFlag(Boolean currFlag) {
		this.currFlag = currFlag;
	}
	public Boolean getRunningFlag() {
		return runningFlag;
	}
	public void setRunningFlag(Boolean runningFlag) {
		this.runningFlag = runningFlag;
	}
	public Boolean getFinishFlag() {
		return finishFlag;
	}
	public void setFinishFlag(Boolean finishFlag) {
		this.finishFlag = finishFlag;
	}
	public Boolean getStateType() {
		return stateType;
	}
	public void setStateType(Boolean stateType) {
		this.stateType = stateType;
	}

	}
