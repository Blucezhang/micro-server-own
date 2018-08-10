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
import javax.persistence.Transient;

@Data
@Entity
@Table(name="SMS")
public class Sms implements Serializable, IDomainBase{
	

	@Id
	@Column(name="id", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	private String sendName;
	private String sendMobile;
	private String receiveName;
	private String receiveMobile;
	private String title;
	private String content;
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date receiveTime;
	private int msgType;
	private int sendStatus ;
	private String sendResult;
	private String thirdId;
	private String thirdStatus;
	private String thirdResult;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;
	private String userName;
	private String userAccount;
	private String sendXML;
	private String receiveXML;
	private int draft;//是否草稿，0否，1是
	private int flag;//是否删除，0否，1是
	private String string1;
	private String string2;
	private String string3;
	private String string4;

	@Transient
	public Object getObjectId() {
		// TODO Auto-generated method stub
		return this.id;
	}
	
	
}
