package workflow.domain;



import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

 

@Entity
@Table(name="Biz_Operation")
public class BizOperation implements Serializable {

	private static final long serialVersionUID = -4067422147574393393L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private String operationId;

	private Long functionId;
	private Long processId;

	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;

	@Temporal(TemporalType.TIMESTAMP)
	private Date exeTime;

	private String operRole;
	private String exeFlag;
	private Long orginOrg;
	private String operLoginId;
	private String actionName;
	private String actionDesc;
	private String functionName;
	private String nextState;
	private String finishFlag;
	private String signFlag;

	public String getOperationId() {
		return operationId;
	}
	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getExeTime() {
		return exeTime;
	}
	public void setExeTime(Date exeTime) {
		this.exeTime = exeTime;
	}
	public String getOperRole() {
		return operRole;
	}
	public void setOperRole(String operRole) {
		this.operRole = operRole;
	}
	public String getExeFlag() {
		return exeFlag;
	}
	public void setExeFlag(String exeFlag) {
		this.exeFlag = exeFlag;
	}
	public Long getOrginOrg() {
		return orginOrg;
	}
	public void setOrginOrg(Long orginOrg) {
		this.orginOrg = orginOrg;
	}
	public String getOperLoginId() {
		return operLoginId;
	}
	public void setOperLoginId(String operLoginId) {
		this.operLoginId = operLoginId;
	}
	public String getActionName() {
		return actionName;
	}
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	public String getActionDesc() {
		return actionDesc;
	}
	public void setActionDesc(String actionDesc) {
		this.actionDesc = actionDesc;
	}
	public String getFunctionName() {
		return functionName;
	}
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	public String getNextState() {
		return nextState;
	}
	public void setNextState(String nextState) {
		this.nextState = nextState;
	}
	public String getFinishFlag() {
		return finishFlag;
	}
	public void setFinishFlag(String finishFlag) {
		this.finishFlag = finishFlag;
	}
	public String getSignFlag() {
		return signFlag;
	}
	public void setSignFlag(String signFlag) {
		this.signFlag = signFlag;
	}

	}
