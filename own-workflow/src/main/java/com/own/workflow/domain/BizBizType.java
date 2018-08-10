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
@Table(name="Biz_BizType")
public class BizBizType implements Serializable {


	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private String bizTypesId;
	private Long processId;
	private Long bizTypeId;
	private String note;
	private Boolean currFlag;
	@Temporal(TemporalType.TIMESTAMP)
	private Date updateTime;
}
