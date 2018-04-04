package com.own.face.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderDetailBean extends IFPageRequest{
	
	private Long orderDetailId;
	private List<Long> orderDetailIdList;
	private Long orderId;
	private Long productId;
	private String productName;
	private Integer productQuantity;
	private BigDecimal productPrice;
	private Integer orderDetailState;
	private String receiptAddress;
	private List<BuySellRelationBean> buySellRelation;
	private LogisticsOrderBean logisticsOrderBean;//在创建物流单的时候使用

}
