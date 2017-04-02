package face.order;

import java.math.BigDecimal;
import java.util.List;

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

	public Long getOrderDetailId() {
		return orderDetailId;
	}
	public void setOrderDetailId(Long orderDetailId) {
		this.orderDetailId = orderDetailId;
	}
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public Long getProductId() {
		return productId;
	}
	public void setProductId(Long productId) {
		this.productId = productId;
	}
	public Integer getOrderDetailState() {
		return orderDetailState;
	}
	public void setOrderDetailState(Integer orderDetailState) {
		this.orderDetailState = orderDetailState;
	}
	public List<BuySellRelationBean> getBuySellRelation() {
		return buySellRelation;
	}
	public void setBuySellRelation(List<BuySellRelationBean> buySellRelation) {
		this.buySellRelation = buySellRelation;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public Integer getProductQuantity() {
		return productQuantity;
	}
	public void setProductQuantity(Integer productQuantity) {
		this.productQuantity = productQuantity;
	}
	public BigDecimal getProductPrice() {
		return productPrice;
	}
	public void setProductPrice(BigDecimal productPrice) {
		this.productPrice = productPrice;
	}
	public List<Long> getOrderDetailIdList() {
		return orderDetailIdList;
	}
	public void setOrderDetailIdList(List<Long> orderDetailIdList) {
		this.orderDetailIdList = orderDetailIdList;
	}
	public LogisticsOrderBean getLogisticsOrderBean() {
		return logisticsOrderBean;
	}
	public void setLogisticsOrderBean(LogisticsOrderBean logisticsOrderBean) {
		this.logisticsOrderBean = logisticsOrderBean;
	}
	public String getReceiptAddress() {
		return receiptAddress;
	}
	public void setReceiptAddress(String receiptAddress) {
		this.receiptAddress = receiptAddress;
	}

}
