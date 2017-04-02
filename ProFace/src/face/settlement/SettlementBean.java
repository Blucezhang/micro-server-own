package face.settlement;

import java.math.BigDecimal;
import java.util.Date;

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
	
	public Long getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(Long paymentId) {
		this.paymentId = paymentId;
	}
	public Long getAcctId() {
		return acctId;
	}
	public void setAcctId(Long acctId) {
		this.acctId = acctId;
	}
	public String getContractNo() {
		return contractNo;
	}
	public void setContractNo(String contractNo) {
		this.contractNo = contractNo;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getPayeeAccNo() {
		return payeeAccNo;
	}
	public void setPayeeAccNo(String payeeAccNo) {
		this.payeeAccNo = payeeAccNo;
	}
	public String getPayeeAccType() {
		return payeeAccType;
	}
	public void setPayeeAccType(String payeeAccType) {
		this.payeeAccType = payeeAccType;
	}
	public String getPayeeAccDept() {
		return payeeAccDept;
	}
	public void setPayeeAccDept(String payeeAccDept) {
		this.payeeAccDept = payeeAccDept;
	}
	public BigDecimal getPayAmt() {
		return payAmt;
	}
	public void setPayAmt(BigDecimal payAmt) {
		this.payAmt = payAmt;
	}
	public Date getPayDate() {
		return payDate;
	}
	public void setPayDate(Date payDate) {
		this.payDate = payDate;
	}
	public Long getOperator() {
		return operator;
	}
	public void setOperator(Long operator) {
		this.operator = operator;
	}
	

}
