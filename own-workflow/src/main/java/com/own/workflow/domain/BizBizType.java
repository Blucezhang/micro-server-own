package com.own.workflow.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

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
