package promotion.action.bean;

import java.util.Date;

public class RuleBaseBean {
	
	private Long id;
	
	private String saleName;

	private Date startTime;
	
	private Date endTime;
	
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
	
	private boolean allJoin;//是否全场参加
	
	private int promotionTypeId;//关联促销类型的id
	
	private int scopeId;//关联促销范围的id

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

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

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
	
	
	
}
