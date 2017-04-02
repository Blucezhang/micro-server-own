package face.settlement;

import java.math.BigDecimal;

import face.order.IFPageRequest;

public class AcctBean extends IFPageRequest{

	private Long acctId;
	private Long partyId;
	private Integer acctType;
	private String acctDept;
	private BigDecimal amount;
	
	public Long getPartyId() {
		return partyId;
	}
	public void setPartyId(Long partyId) {
		this.partyId = partyId;
	}
	public Long getAcctId() {
		return acctId;
	}
	public void setAcctId(Long acctId) {
		this.acctId = acctId;
	}
	public Integer getAcctType() {
		return acctType;
	}
	public void setAcctType(Integer acctType) {
		this.acctType = acctType;
	}
	public String getAcctDept() {
		return acctDept;
	}
	public void setAcctDept(String acctDept) {
		this.acctDept = acctDept;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
}
