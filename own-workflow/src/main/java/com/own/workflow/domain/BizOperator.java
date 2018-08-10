package com.own.workflow.domain;

import lombok.Data;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name="Biz_Operator")
public class BizOperator implements Serializable,IDomainBase {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long operatorId;
	private Long convertId;
 	private Long loginUserId;
	@Override
	public Object getObjectId() {
		 
		return convertId;
	}
}
