package com.own.owncommon.ems;

import lombok.Data;

import java.util.Date;

@Data
public class Email {
    private int id;
    private String sendName;
    private String sendEmail;
    private String receiveName;
    private String receiveEmail;
    private String title;
    private String content;
    private Date sendTime;
    private Date receiveTime;
    private int emailType;
    private int sendStatus;
    private String sendResult;
    private String thirdId;
    private String thirdStatus;
    private String thirdResult;
    private Date createTime;
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
    private int flag;
    private String string1;
    private String string2;
    private String string3;
    private String string4;

}
