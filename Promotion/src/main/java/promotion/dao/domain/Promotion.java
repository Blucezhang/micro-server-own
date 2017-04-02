package promotion.dao.domain;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity
public class Promotion extends BasePromotion {
	
	
/**
	 * 
	 */
	private static final long serialVersionUID = 4814067601197564210L;

	@Property(name="integral")
	private int integral; //赠送积分
	
	@Property(name="straightDown")
	private Double straightDown;//直降**元
	
	@Property(name="disCount")
	private Double disCount;//折扣
	
	@Property(name="timeLimit")
	private boolean timeLimit;//限时
	
	@Property(name="amountLimit")
	private boolean amountLimit;//限量
	
	@Property(name="oneLimit")
	private boolean oneLimit;//限购一个
	
	

	public int getIntegral() {
		return integral;
	}

	public void setIntegral(int integral) {
		this.integral = integral;
	}

	

	public Double getStraightDown() {
		return straightDown;
	}

	public void setStraightDown(Double straightDown) {
		this.straightDown = straightDown;
	}

	

	

	public Double getDisCount() {
		return disCount;
	}

	public void setDisCount(Double disCount) {
		this.disCount = disCount;
	}

	public boolean isTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(boolean timeLimit) {
		this.timeLimit = timeLimit;
	}

	public boolean isAmountLimit() {
		return amountLimit;
	}

	public void setAmountLimit(boolean amountLimit) {
		this.amountLimit = amountLimit;
	}

	public boolean isOneLimit() {
		return oneLimit;
	}

	public void setOneLimit(boolean oneLimit) {
		this.oneLimit = oneLimit;
	}

	

	
	
	
	
}
