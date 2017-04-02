package promotion.dao.domain;

public class SetPromotion extends BasePromotion{

	private static final long serialVersionUID = 1L;
	
	private Integer singleBuyCount;
	private boolean oneLimit;
	
	public Integer getSingleBuyCount() {
		return singleBuyCount;
	}
	public void setSingleBuyCount(Integer singleBuyCount) {
		this.singleBuyCount = singleBuyCount;
	}
	public boolean isOneLimit() {
		return oneLimit;
	}
	public void setOneLimit(boolean oneLimit) {
		this.oneLimit = oneLimit;
	}
	
	
	
}
