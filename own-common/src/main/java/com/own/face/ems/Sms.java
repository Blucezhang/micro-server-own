package com.own.face.ems;

import lombok.Data;

import java.util.Date;

@Data
public class Sms {
	private Integer id;
	private String mobile;
	private String title;
	private String content;
	private String sendName;
	private String sendMobile;
	private String receiveName;
	private String receiveMobile;
	private Date sendTime;
	private Date receiveTime;
	private int msgType;
	private int sendStatus ;
	private String sendResult;
	private String thirdId;
	private String thirdStatus;
	private String thirdResult;
	private Date createTime;
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
}
