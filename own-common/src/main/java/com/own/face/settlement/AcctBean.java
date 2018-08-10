package com.own.face.settlement;

import com.own.face.order.IFPageRequest;
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
