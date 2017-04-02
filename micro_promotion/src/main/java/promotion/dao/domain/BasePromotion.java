package promotion.dao.domain;

import java.io.Serializable;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity
public class BasePromotion implements Serializable{
	
	private static final long serialVersionUID = 6635706726587689115L;

	@GraphId
	private Long id;
	
	@Property(name="saleName")
	private String saleName;

	@Property(name="startTime")
	private String startTime;
//	
	@Property(name="endTime")
	private String endTime;
	
	@Property(name="slogan")
	private String slogan;//广告语
	
	@Property(name="saleAddress")
	private String saleAddress;
	
	@Property(name="saleMark")
	private String saleMark;
	
	@Property(name="createTime")
	private String createTime;
	
	@Property(name="saleStatus")
	private int saleStatus;//活动状态
	
	@Property(name="examineStatus")
	private int examineStatus;//审核状态
	
	@Property(name="userName")
	private String userName;
	
	@Property(name="userAccount")
	private String userAccount;
	
	@Property(name="userRole")
	private String userRole;
	
	@Property(name="flag")
	private int flag;
	
	@Property(name="allJoin")
	private boolean allJoin;





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


//
//	public Date getEndTime() {
//		return endTime;
//	}
//
//	public void setEndTime(Date endTime) {
//		this.endTime = endTime;
//	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
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

	

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
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
	
	
	
}
