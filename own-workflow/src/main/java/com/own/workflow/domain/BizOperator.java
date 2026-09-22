package com.own.workflow.domain;

import lombok.Data;

import java.io.Serializable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
