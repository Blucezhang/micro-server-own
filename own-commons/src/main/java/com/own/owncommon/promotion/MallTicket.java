package com.own.owncommon.promotion;

import lombok.Data;

import java.util.Date;

@Data
public class MallTicket {
	private Long id;
	private String ticketName;
	private int number;
	private int full;
	private int minux;
	private int sendTicketType;
	private int receiveTicketType;
	private int level;
	private Date receiveStartTime;
	private Date receivieEndTime;
	private Date applyStartTime;
	private Date applyEndTime;
	private Date createTicketTime;
	private int status;
	private int checkStatus;
	private int haveSendTicket;
	private int haveApplyTicket;
	private int flag;
	private int value;

}
