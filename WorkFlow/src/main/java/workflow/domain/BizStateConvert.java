package workflow.domain;



import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Biz_StateConvert")
public class BizStateConvert implements Serializable,IDomainBase {
	@Id
	@Column(name = "ConvertId", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long convertId;
	private Integer bizTypeId;
	private Long partyId;
	private Long functionId;
	private Boolean sysAutoFlag;
	private Integer strategyType;
  	 
	private Integer preState;
	private Integer nextStatus;
	
	public Long getConvertId() {
		return convertId;
	}

	public void setConvertId(Long convertId) {
		this.convertId = convertId;
	}

	public Integer getBizTypeId() {
		return bizTypeId;
	}

	public void setBizTypeId(Integer bizTypeId) {
		this.bizTypeId = bizTypeId;
	}

	public Long getPartyId() {
		return partyId;
	}

	public void setPartyId(Long partyId) {
		this.partyId = partyId;
	}

	public Long getFunctionId() {
		return functionId;
	}

	public void setFunctionId(Long functionId) {
		this.functionId = functionId;
	}

	public Boolean getSysAutoFlag() {
		return sysAutoFlag;
	}

	public void setSysAutoFlag(Boolean sysAutoFlag) {
		this.sysAutoFlag = sysAutoFlag;
	}

	public Integer getStrategyType() {
		return strategyType;
	}

	public void setStrategyType(Integer strategyType) {
		this.strategyType = strategyType;
	}
  
	public Integer getPreState() {
		return preState;
	}

	public void setPreState(Integer preState) {
		this.preState = preState;
	}

	public Integer getNextStatus() {
		return nextStatus;
	}

	public void setNextStatus(Integer nextStatus) {
		this.nextStatus = nextStatus;
	}


	 
		public Object getObjectId() {
 			return convertId;
		}
		 
 
		
}
