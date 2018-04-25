package com.own.workflow.domain;
 
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
 

@Entity
@Table(name="View_ProcConvert")
public class FlowView implements Serializable,IDomainBase {
 	private Long processId;
 	private Long prodId;
 	
	@Id
	@Column(name = "convertId", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long convertId;

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
		public Long getProdId() {
			return prodId;
		}
		public void setProdId(Long prodId) {
			this.prodId = prodId;
		}
		public Long getProcessId() {
			return processId;
		}
		public void setProcessId(Long processId) {
			this.processId = processId;
		}
		public Integer getBizType() {
			return bizType;
		}
		public void setBizType(Integer bizType) {
			this.bizType = bizType;
		}
		public String getBizName() {
			return bizName;
		}
		public void setBizName(String bizName) {
			this.bizName = bizName;
		}
		public String getDistrict() {
			return district;
		}
		public void setDistrict(String district) {
			this.district = district;
		}
		public Long getOriginOrg() {
			return originOrg;
		}
		public void setOriginOrg(Long originOrg) {
			this.originOrg = originOrg;
		}
		public Integer getStatus() {
			return status;
		}
		public void setStatus(Integer status) {
			this.status = status;
		}
 
		public Long getPartyId() {
			return partyId;
		}
		public void setPartyId(Long partyId) {
			this.partyId = partyId;
		}
		public Long getFunctionId() {
			return functionId;
		}
		public void setFunctionId(Long functionId) {
			this.functionId = functionId;
		}
		public Boolean getSysAutoFlag() {
			return sysAutoFlag;
		}
		public void setSysAutoFlag(Boolean sysAutoFlag) {
			this.sysAutoFlag = sysAutoFlag;
		}
 
		public Integer getNextStatus() {
			return nextStatus;
		}
		public void setNextStatus(Integer nextStatus) {
			this.nextStatus = nextStatus;
		}
  		@Override
		public Object getObjectId() {
 			return processId;
		}
		public String getFunName() {
			return funName;
		}
		public void setFunName(String funName) {
			this.funName = funName;
		}
		
		
}
