package workflow.action.bean;

import java.util.Map;

import javax.persistence.Transient;

import workflow.util.Util;

public class BizBusinessFlowBean {
	
	private Integer bizTypeId;
	
	private Long processId;
	
	private String businessflowContext;

	public Integer getBizTypeId() {
		return bizTypeId;
	}

	public void setBizTypeId(Integer bizTypeId) {
		this.bizTypeId = bizTypeId;
	}

	public Long getProcessId() {
		return processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}

	public String getBusinessflowContext() {
		return businessflowContext;
	}

	public void setBusinessflowContext(String businessflowContext) {
		this.businessflowContext = businessflowContext;
	}
	
	
	@Transient
	public Map<?, ?> getbusinessflowContextValue() {
		return (Map<?, ?>) Util.createJson2Bean(this.businessflowContext, Map.class);
	}

	

}
