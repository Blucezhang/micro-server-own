package com.own.face.settlement;

import lombok.Data;

import java.math.BigDecimal;


@Data
public class AcctBean extends IFPageRequest {

	private Long acctId;
	private Long partyId;
	private Integer acctType;
	private String acctDept;
	private BigDecimal amount;
}
