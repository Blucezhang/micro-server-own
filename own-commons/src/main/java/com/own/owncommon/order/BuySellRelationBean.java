package com.own.owncommon.order;

import lombok.Data;

@Data
public class BuySellRelationBean {
	
	private Long buyerSellerId;
	private BuyerBean buyers;
	private SellerBean sellers;
}
