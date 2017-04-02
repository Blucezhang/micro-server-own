package face.order;

import java.util.Date;

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
	
	public Long getLogisticsOrderId() {
		return logisticsOrderId;
	}
	public void setLogisticsOrderId(Long logisticsOrderId) {
		this.logisticsOrderId = logisticsOrderId;
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
	public String getLogisticsNo() {
		return logisticsNo;
	}
	public void setLogisticsNo(String logisticsNo) {
		this.logisticsNo = logisticsNo;
	}
	public String getReceiver() {
		return Receiver;
	}
	public void setReceiver(String receiver) {
		Receiver = receiver;
	}
	public String getDeliver() {
		return deliver;
	}
	public void setDeliver(String deliver) {
		this.deliver = deliver;
	}
	public String getProductName() {
		return ProductName;
	}
	public void setProductName(String productName) {
		ProductName = productName;
	}
	public Integer getProductQuantity() {
		return productQuantity;
	}
	public void setProductQuantity(Integer productQuantity) {
		this.productQuantity = productQuantity;
	}
	public String getReceiptAddress() {
		return receiptAddress;
	}
	public void setReceiptAddress(String receiptAddress) {
		this.receiptAddress = receiptAddress;
	}
	public String getReceiverPhone() {
		return receiverPhone;
	}
	public void setReceiverPhone(String receiverPhone) {
		this.receiverPhone = receiverPhone;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	public Date getDeliveryStartDate() {
		return deliveryStartDate;
	}
	public void setDeliveryStartDate(Date deliveryStartDate) {
		this.deliveryStartDate = deliveryStartDate;
	}
	public Date getDeliveryEndDate() {
		return deliveryEndDate;
	}
	public void setDeliveryEndDate(Date deliveryEndDate) {
		this.deliveryEndDate = deliveryEndDate;
	}
	
}
