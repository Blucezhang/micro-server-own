package com.own.send.server.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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
		// TODO Auto-generated method stub
		return this.id;
	}
	
	
	
}
