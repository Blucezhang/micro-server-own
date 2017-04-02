package promotion.dao.domain;

import java.util.Date;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class MallTicket {

	@GraphId
	private Long id;
	private String ticketName;
	private int number;
	private int full;
	private int minux;
	private int sendTicketType;
	private int receiveTicketType;
	private int level;
	private Date receiveStartTime;
	private Date receivieEndTime;
	private Date applyStartTime;
	private Date applyEndTime;
	private Date createTicketTime;
	private int status;
	private int checkStatus;
	private int haveSendTicket;
	private int haveApplyTicket;
	private int flag;
	private int value;
	private String userAccount;
	private String userName;
	private String userRole;
	
	
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTicketName() {
		return ticketName;
	}
	public void setTicketName(String ticketName) {
		this.ticketName = ticketName;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getFull() {
		return full;
	}
	public void setFull(int full) {
		this.full = full;
	}
	public int getMinux() {
		return minux;
	}
	public void setMinux(int minux) {
		this.minux = minux;
	}
	public int getSendTicketType() {
		return sendTicketType;
	}
	public void setSendTicketType(int sendTicketType) {
		this.sendTicketType = sendTicketType;
	}
	public int getReceiveTicketType() {
		return receiveTicketType;
	}
	public void setReceiveTicketType(int receiveTicketType) {
		this.receiveTicketType = receiveTicketType;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public Date getReceiveStartTime() {
		return receiveStartTime;
	}
	public void setReceiveStartTime(Date receiveStartTime) {
		this.receiveStartTime = receiveStartTime;
	}
	public Date getReceivieEndTime() {
		return receivieEndTime;
	}
	public void setReceivieEndTime(Date receivieEndTime) {
		this.receivieEndTime = receivieEndTime;
	}
	public Date getApplyStartTime() {
		return applyStartTime;
	}
	public void setApplyStartTime(Date applyStartTime) {
		this.applyStartTime = applyStartTime;
	}
	public Date getApplyEndTime() {
		return applyEndTime;
	}
	public void setApplyEndTime(Date applyEndTime) {
		this.applyEndTime = applyEndTime;
	}
	public Date getCreateTicketTime() {
		return createTicketTime;
	}
	public void setCreateTicketTime(Date createTicketTime) {
		this.createTicketTime = createTicketTime;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getCheckStatus() {
		return checkStatus;
	}
	public void setCheckStatus(int checkStatus) {
		this.checkStatus = checkStatus;
	}
	public int getHaveSendTicket() {
		return haveSendTicket;
	}
	public void setHaveSendTicket(int haveSendTicket) {
		this.haveSendTicket = haveSendTicket;
	}
	public int getHaveApplyTicket() {
		return haveApplyTicket;
	}
	public void setHaveApplyTicket(int haveApplyTicket) {
		this.haveApplyTicket = haveApplyTicket;
	}
	public int getFlag() {
		return flag;
	}
	public void setFlag(int flag) {
		this.flag = flag;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getUserAccount() {
		return userAccount;
	}
	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserRole() {
		return userRole;
	}
	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}
	
	
	
}
