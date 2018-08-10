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
@Table(name="EMAIL")
public class Email implements Serializable, IDomainBase{
	

	@Id
	@Column(name="id", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	private String sendName;
	private String sendEmail;
	private String receiveName;
	private String receiveEmail;
	private String title;
	private String content;
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date receiveTime;
	private int emailType;
	private int sendStatus;
	private String sendResult;
	private String thirdId;
	private String thirdStatus;
	private String thirdResult;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;
	private String imgUrl;
	private String fileUrl;
	private String videoUrl;
	private String musicUrl;
	private String userName;
	private String userAccount;
	private String sendXML;
	private String receiveXML;
	private int draft;
	private int flag;//是否删除，0否，1是
	private String string1;
	private String string2;
	private String string3;
	private String string4;

	@Override
	public Object getObjectId() {
		// TODO Auto-generated method stub
		return this.id;
	}
	
	
}
