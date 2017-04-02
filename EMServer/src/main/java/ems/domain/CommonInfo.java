package ems.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="COMMONINFO")
public class CommonInfo implements Serializable, IDomainBase{
	
	@Id
	@Column(name="id",unique=true,nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	
	private int infoId;
	private String infoType;
	private String sendName;
	private String sendAccount;
	private String receiveName;
	private String receiveAccount;
	private String title;
	private String content;
	private int type;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendTime;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date receiveTime;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public int getInfoId() {
		return infoId;
	}
	public void setInfoId(int infoId) {
		this.infoId = infoId;
	}
	public String getInfoType() {
		return infoType;
	}
	public void setInfoType(String infoType) {
		this.infoType = infoType;
	}
	public String getSendName() {
		return sendName;
	}
	public void setSendName(String sendName) {
		this.sendName = sendName;
	}
	public String getSendAccount() {
		return sendAccount;
	}
	public void setSendAccount(String sendAccount) {
		this.sendAccount = sendAccount;
	}
	public String getReceiveName() {
		return receiveName;
	}
	public void setReceiveName(String receiveName) {
		this.receiveName = receiveName;
	}
	public String getReceiveAccount() {
		return receiveAccount;
	}
	public void setReceiveAccount(String receiveAccount) {
		this.receiveAccount = receiveAccount;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Date getSendTime() {
		return sendTime;
	}
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
	public Date getReceiveTime() {
		return receiveTime;
	}
	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}
	@Override
	public Object getObjectId() {
		// TODO Auto-generated method stub
		return this.id;
	}
	
	
	
}
