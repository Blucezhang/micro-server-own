package com.own.workflow.domain;

import com.own.face.util.Util;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;


import javax.persistence.*;


@Entity
@Table(name="biz_businessflowcontext")
@Data
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

	@Override
	public Object getObjectId() {
		// TODO Auto-generated method stub
		return null;
	}
}
