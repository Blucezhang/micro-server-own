package com.own.face.ems;

import lombok.Data;

import java.util.Date;

@Data
public class Jgpush {

    private Integer id;
    private String title;
    private String content;
    private Integer platform;
    private String receiveId;
    private String sendId;
    private String releaseFun;
    private Date sendTime;
    private Integer jpushType;
    private Integer sendStatus;
    private String sendResult;
    private String receiveResult;
    private Date receiveTime;
    private Integer receiveStatus;
    private Date createTime;
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

}
