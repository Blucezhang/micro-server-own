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

@Entity
@Table(name="Biz_BizType")
@Data
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
}
