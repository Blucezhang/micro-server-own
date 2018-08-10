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
		// TODO Auto-generated method stub
		return this.id;
	}
	
	
	
}
