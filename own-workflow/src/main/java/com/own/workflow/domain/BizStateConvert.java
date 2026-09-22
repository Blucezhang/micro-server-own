package com.own.workflow.domain;



import lombok.Data;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Entity
@Table(name="Biz_StateConvert")
public class BizStateConvert implements Serializable,IDomainBase {
	@Id
	@Column(name = "ConvertId", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long convertId;
	private Integer bizTypeId;
	private Long partyId;
	private Long functionId;
	private Boolean sysAutoFlag;
	private Integer strategyType;
  	 
	private Integer preState;
	private Integer nextStatus;
	public Object getObjectId() {
 			return convertId;
		}
		 
 
		
}
