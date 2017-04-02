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
@Table(name="JGPUSH")
public class Jgpush implements Serializable, IDomainBase{
	

	@Id
	@Column(unique=true,nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id ;
	
	private String title;
	private String content;
	private Integer platform;
	private String receiveId;
	private String sendId;
	private String releaseFun;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendTime;
	private Integer jpushType;
	private Integer sendStatus;
	private String sendResult;
	private String receiveResult;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date receiveTime;
	private Integer receiveStatus;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;
	private String userName;
	private String userAccount;
	private String sendXML;
	private String receiveXML;
	private Integer flag;
	private Integer drapt;
	private String string1;
	private String string2;
	private String string3;
	private String string4;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
	public Integer getPlatform() {
		return platform;
	}
	public void setPlatform(Integer platform) {
		this.platform = platform;
	}
	public String getReceiveId() {
		return receiveId;
	}
	public void setReceiveId(String receiveId) {
		this.receiveId = receiveId;
	}
	public String getSendId() {
		return sendId;
	}
	public void setSendId(String sendId) {
		this.sendId = sendId;
	}
	public String getReleaseFun() {
		return releaseFun;
	}
	public void setReleaseFun(String releaseFun) {
		this.releaseFun = releaseFun;
	}
	public Date getSendTime() {
		return sendTime;
	}
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
	public Integer getJpushType() {
		return jpushType;
	}
	public void setJpushType(Integer jpushType) {
		this.jpushType = jpushType;
	}
	public Integer getSendStatus() {
		return sendStatus;
	}
	public void setSendStatus(Integer sendStatus) {
		this.sendStatus = sendStatus;
	}
	public String getSendResult() {
		return sendResult;
	}
	public void setSendResult(String sendResult) {
		this.sendResult = sendResult;
	}
	public String getReceiveResult() {
		return receiveResult;
	}
	public void setReceiveResult(String receiveResult) {
		this.receiveResult = receiveResult;
	}
	public Date getReceiveTime() {
		return receiveTime;
	}
	public void setReceiveTime(Date receiveTime) {
		this.receiveTime = receiveTime;
	}
	public Integer getReceiveStatus() {
		return receiveStatus;
	}
	public void setReceiveStatus(Integer receiveStatus) {
		this.receiveStatus = receiveStatus;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
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
	public String getSendXML() {
		return sendXML;
	}
	public void setSendXML(String sendXML) {
		this.sendXML = sendXML;
	}
	public String getReceiveXML() {
		return receiveXML;
	}
	public void setReceiveXML(String receiveXML) {
		this.receiveXML = receiveXML;
	}
	public Integer getFlag() {
		return flag;
	}
	public void setFlag(Integer flag) {
		this.flag = flag;
	}
	public Integer getDrapt() {
		return drapt;
	}
	public void setDrapt(Integer drapt) {
		this.drapt = drapt;
	}
	public String getString1() {
		return string1;
	}
	public void setString1(String string1) {
		this.string1 = string1;
	}
	public String getString2() {
		return string2;
	}
	public void setString2(String string2) {
		this.string2 = string2;
	}
	public String getString3() {
		return string3;
	}
	public void setString3(String string3) {
		this.string3 = string3;
	}
	public String getString4() {
		return string4;
	}
	public void setString4(String string4) {
		this.string4 = string4;
	}
	
	@Override
	public Object getObjectId() {
		// TODO Auto-generated method stub
		return this.id;
	}
	
	
	
}
