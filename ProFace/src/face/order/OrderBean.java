package face.order;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class OrderBean {

	private Long orderId;
	private String orderNo;
	private Integer orderState;
	private Integer payType;
	private Date createDate;
	private List<OrderDetailBean> orderDetail;
	private BigDecimal totalPrice;
	
	public BigDecimal getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public Integer getOrderState() {
		return orderState;
	}
	public void setOrderState(Integer orderState) {
		this.orderState = orderState;
	}
	public Integer getPayType() {
		return payType;
	}
	public void setPayType(Integer payType) {
		this.payType = payType;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public List<OrderDetailBean> getOrderDetail() {
		return orderDetail;
	}
	public void setOrderDetail(List<OrderDetailBean> orderDetail) {
		this.orderDetail = orderDetail;
	}
	
}
