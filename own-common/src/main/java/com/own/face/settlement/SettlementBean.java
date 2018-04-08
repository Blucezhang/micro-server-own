package com.own.face.settlement;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class SettlementBean {
	
	private Long paymentId;
	private Long acctId;
	private String contractNo;
	private String orderNo;
	private String payeeAccNo;
	private String payeeAccType;
	private String payeeAccDept;
	private BigDecimal payAmt;
	private Date payDate;
	private Long operator;

}
