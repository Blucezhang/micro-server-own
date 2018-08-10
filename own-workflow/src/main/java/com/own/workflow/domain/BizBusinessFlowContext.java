package com.own.workflow.domain;

import com.own.face.util.Util;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;


import javax.persistence.*;

@Data
@Entity
@Table(name="biz_businessflowcontext")
public class BizBusinessFlowContext implements Serializable,IDomainBase {

	public BizBusinessFlowContext() {
		super();
	}

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

	@Override
	public Object getObjectId() {
		return null;
	}
}
