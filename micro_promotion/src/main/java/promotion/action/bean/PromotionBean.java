package promotion.action.bean;

import java.util.Date;

/**
 * 暂时匹配单品促销，其他促销后续使用java引擎规则实现
 * @author sjnl
 *
 */
public class PromotionBean extends RuleBaseBean{
	
	private Long id;
	
	private String saleName;

//	private Date startTime;
//	
//	private Date endTime;
	
	private String slogan;//广告语
	
	private String saleAddress;
	
	private String saleMark;
	
	private Date createTime;
	
	private int saleStatus;//活动状态
	
	private int examineStatus;//审核状态
	
	private String userName;
	
	private String userAccount;
	
	private String userRole;
	
	private int flag;
	
	private boolean allJoin;
	
	private int integral; //赠送积分
	
	private String mallTicketId;//关联商城券id
	
	private String storeTicketId;//关联店铺券id
	
	private Double straightDown;//直降**元
	
	private Double disCount;//折扣
	
	private int promotionTypeId;//关联促销类型的id
	
	private int scopeId;//关联促销范围的id

	private String productJson;//参与促销的商品信息
	
	private boolean timeLimit;//限时
	
	private boolean amountLimit;//限量
	
	private boolean oneLimit;//限购一个
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSaleName() {
		return saleName;
	}

	public void setSaleName(String saleName) {
		this.saleName = saleName;
	}

//	public Date getStartTime() {
//		return startTime;
//	}
//
//	public void setStartTime(Date startTime) {
//		this.startTime = startTime;
//	}
//
//	public Date getEndTime() {
//		return endTime;
//	}
//
//	public void setEndTime(Date endTime) {
//		this.endTime = endTime;
//	}

	public String getSlogan() {
		return slogan;
	}

	public void setSlogan(String slogan) {
		this.slogan = slogan;
	}

	public String getSaleAddress() {
		return saleAddress;
	}

	public void setSaleAddress(String saleAddress) {
		this.saleAddress = saleAddress;
	}

	public String getSaleMark() {
		return saleMark;
	}

	public void setSaleMark(String saleMark) {
		this.saleMark = saleMark;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getSaleStatus() {
		return saleStatus;
	}

	public void setSaleStatus(int saleStatus) {
		this.saleStatus = saleStatus;
	}

	public int getExamineStatus() {
		return examineStatus;
	}

	public void setExamineStatus(int examineStatus) {
		this.examineStatus = examineStatus;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	

	public boolean isAllJoin() {
		return allJoin;
	}

	public void setAllJoin(boolean allJoin) {
		this.allJoin = allJoin;
	}

	public int getPromotionTypeId() {
		return promotionTypeId;
	}

	public void setPromotionTypeId(int promotionTypeId) {
		this.promotionTypeId = promotionTypeId;
	}

	public int getScopeId() {
		return scopeId;
	}

	public void setScopeId(int scopeId) {
		this.scopeId = scopeId;
	}

	public int getIntegral() {
		return integral;
	}

	public void setIntegral(int integral) {
		this.integral = integral;
	}

	public String getMallTicketId() {
		return mallTicketId;
	}

	public void setMallTicketId(String mallTicketId) {
		this.mallTicketId = mallTicketId;
	}

	public String getStoreTicketId() {
		return storeTicketId;
	}

	public void setStoreTicketId(String storeTicketId) {
		this.storeTicketId = storeTicketId;
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

	public String getProductJson() {
		return productJson;
	}

	public void setProductJson(String productJson) {
		this.productJson = productJson;
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
