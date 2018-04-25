package com.own.workflow.domain;

import com.own.face.util.Util;

import java.io.Serializable;
import java.util.Map;


import javax.persistence.*;


@Entity
@Table(name="biz_businessflowcontext")
public class BizBusinessFlowContext implements Serializable,IDomainBase {

	public BizBusinessFlowContext() {
		super();
	}

	private static final long serialVersionUID = -4128030031018430984L;
	
	@Id
	@Column(name = "businessflowId", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer businessflowId;
	
	private Integer bizTypeId;
	
	private Long processId;
	
	private String businessflowContext;

	@Transient
	public Map<?, ?> getbusinessflowContextValue() {
		return (Map<?, ?>) Util.createJson2Bean(this.businessflowContext, Map.class);
	}
	
	public Long getProcessId() {
		return processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}


	public Integer getBusinessflowId() {
		return businessflowId;
	}
	
	public void setBusinessflowId(Integer businessflowId) {
		this.businessflowId = businessflowId;
	}

	public Integer getBizTypeId() {
		return bizTypeId;
	}

	public void setBizTypeId(Integer bizTypeId) {
		this.bizTypeId = bizTypeId;
	}


	public String getBusinessflowContext() {
		return businessflowContext;
	}

	public void setBusinessflowContext(String businessflowContext) {
		this.businessflowContext = businessflowContext;
	}

	@Override
	public Object getObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
}
