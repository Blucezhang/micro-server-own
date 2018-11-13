package com.own.owncommon.order;

import lombok.Data;

import java.util.Date;

@Data
public class LogisticsOrderBean {

	private Long logisticsOrderId;
	private Long orderId;
	private String orderNo;
	private String logisticsNo;
	private String Receiver;
	private String deliver;
	private String ProductName;
	private Integer productQuantity;
	private String receiptAddress;
	private String receiverPhone;
	private String zipCode;
	private Date deliveryStartDate;
	private Date deliveryEndDate;

}
