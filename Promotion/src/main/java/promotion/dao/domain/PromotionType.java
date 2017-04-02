package promotion.dao.domain;

import java.util.Date;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class PromotionType {
	
	@GraphId
	private Long id;
	
	private String saleTypeName;
	
	private String saleTypeAlias;
	
	private Date createTime;

	private String userName;
	
	private String userAccount;
	
	private int flag;
	
	private String saleTypeMark;
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSaleTypeName() {
		return saleTypeName;
	}

	public void setSaleTypeName(String saleTypeName) {
		this.saleTypeName = saleTypeName;
	}

	public String getSaleTypeAlias() {
		return saleTypeAlias;
	}

	public void setSaleTypeAlias(String saleTypeAlias) {
		this.saleTypeAlias = saleTypeAlias;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
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

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public String getSaleTypeMark() {
		return saleTypeMark;
	}

	public void setSaleTypeMark(String saleTypeMark) {
		this.saleTypeMark = saleTypeMark;
	}

	
	
	
	
}
