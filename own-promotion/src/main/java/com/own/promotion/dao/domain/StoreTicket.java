package com.own.promotion.dao.domain;

import java.util.Date;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Node
public class StoreTicket {

	@Id
    @GeneratedValue
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
