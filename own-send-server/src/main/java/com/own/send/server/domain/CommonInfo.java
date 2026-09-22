package com.own.send.server.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Data
@Entity
@Table(name="COMMONINFO")
public class CommonInfo implements Serializable, IDomainBase{
	
	@Id
	@Column(name="id",unique=true,nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	private int infoId;
	private String infoType;
	private String sendName;
	private String sendAccount;
	private String receiveName;
	private String receiveAccount;
	private String title;
	private String content;
	private int type;
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date receiveTime;
	@Override
	public Object getObjectId() {
		return this.id;
	}
	
	
	
}
