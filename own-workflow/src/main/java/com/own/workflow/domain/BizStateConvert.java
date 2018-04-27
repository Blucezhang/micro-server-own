package com.own.workflow.domain;



import lombok.Data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Biz_StateConvert")
@Data
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
