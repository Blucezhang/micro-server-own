package com.own.promotion.dao.domain;

import java.util.Date;

import lombok.Data;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@Data
@NodeEntity
public class MallTicket {

	@GraphId
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
	private String userAccount;
	private String userName;
	private String userRole;
}
