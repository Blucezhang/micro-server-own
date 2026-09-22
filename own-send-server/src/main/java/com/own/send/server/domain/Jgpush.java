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
@Table(name="JGPUSH")
public class Jgpush implements Serializable, IDomainBase{
	

	@Id
	@Column(unique=true,nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id ;
	private String title;
	private String content;
	private Integer platform;
	private String receiveId;
	private String sendId;
	private String releaseFun;
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendTime;
	private Integer jpushType;
	private Integer sendStatus;
	private String sendResult;
	private String receiveResult;
	@Temporal(TemporalType.TIMESTAMP)
	private Date receiveTime;
	private Integer receiveStatus;
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;
	private String userName;
	private String userAccount;
	private String sendXML;
	private String receiveXML;
	private Integer flag;
	private Integer drapt;
	private String string1;
	private String string2;
	private String string3;
	private String string4;
	@Override
	public Object getObjectId() {
		return this.id;
	}
	
	
	
}
