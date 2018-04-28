package com.own.promotion.dao.domain;

import java.util.Date;

import lombok.Data;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@Data
@NodeEntity
public class StoreTicket {

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
	private Date receiveEndTime;
	private Date applyStartTime;
	private Date applyEndTime;
	private Date createTicketTime;
	private int status;
	private int checkStatus;
	private int haveSendTicket;
	private int haveApplyTicket;
	private int flag;
	private String address;
	private int value;//面值
	private String userAccount;
	private String userName;
	private String userRole;
}
