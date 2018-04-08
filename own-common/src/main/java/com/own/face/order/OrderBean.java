package com.own.face.order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderBean {

	private Long orderId;
	private String orderNo;
	private Integer orderState;
	private Integer payType;
	private Date createDate;
	private List<OrderDetailBean> orderDetail;
	private BigDecimal totalPrice;
}
