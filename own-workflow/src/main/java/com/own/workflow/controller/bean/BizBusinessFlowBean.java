package com.own.workflow.controller.bean;
import com.own.face.util.Util;
import lombok.Data;

import java.util.Map;

import javax.persistence.Transient;

/**
 * @author BluceZhang
 */

@Data
public class BizBusinessFlowBean {

	private Integer bizTypeId;
	private Long processId;
	private String businessflowContext;

	@Transient
	public Map<?, ?> getbusinessflowContextValue() {
		return (Map<?, ?>) Util.createJson2Bean(this.businessflowContext, Map.class);
	}
}
