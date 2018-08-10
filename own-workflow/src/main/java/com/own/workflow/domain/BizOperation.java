package com.own.workflow.domain;



import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Data
@Entity
@Table(name="Biz_Operation")
public class BizOperation implements Serializable {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private String operationId;
	private Long functionId;
	private Long processId;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date exeTime;
	private String operRole;
	private String exeFlag;
	private Long orginOrg;
	private String operLoginId;
	private String actionName;
	private String actionDesc;
	private String functionName;
	private String nextState;
	private String finishFlag;
	private String signFlag;
}
