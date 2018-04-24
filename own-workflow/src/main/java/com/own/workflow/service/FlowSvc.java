package com.own.workflow.service;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.workflow.controller.bean.BizBusinessFlowBean;
import com.own.workflow.domain.BizBusinessFlowContext;
import com.own.workflow.domain.BizState;
import com.own.workflow.domain.FlowView;
import com.own.workflow.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


/*
 * View_ProcOper中同一个Process应该具有多条记录，每个人一条记录
 * 
 * 
 * 
 */
@Service
public class FlowSvc {

	@Autowired
	private RdbSvc rdbSvc;

	// 获取所有可以由用户执行的流程
	private String getUserAction = "from FlowView vp,BizOperator bp where bp.loginUserId=:loginId and vp.convertId=bp.convertId";
	private String getCurrStateView = "from FlowView where processId=:processId and FunName=:funName and prestatus=status";
	private String updateCurrStateSql = "update Biz_State set currFlag= false where processId=:processId and currFlag = true";
	private String getUserTransByPartyIdAndBizTypeId = "from FlowView vp where vp.partyId=:partyId and vp.bizType=:bizTypeId";

	private String getAllTransType = "from BizType";

	/**
	 * 根据调用的功能名称设置下一个状态
	 * 
	 * @param processId
	 * @param params
	 */
	public Object doFlow(Map params) {

		if (params.get("processId") == null){
			System.out.println("流程处理的Id不能为空！！！");
		}
			// throw new IFException("流程处理的Id不能为空！！！");
		if(params.get("funName")==null){
			System.out.println("工作流名称不能为空");
		}
		// 1.获取处理流程的当前状态
		FlowView bizView = getUniqueFlowView(Long.parseLong(params.get("processId").toString()), params.get("funName").toString().trim());
		Integer nextState = bizView.getNextStatus();
		// 2.变更状态
		BizState bizState=(BizState) changeState(Long.parseLong(params.get("processId").toString()), nextState);
		
		return bizState;
	}

	/**
	 * 变更流程的当前状态
	 * 
	 * @param processId
	 * @param nextState
	 */
	private Object changeState(Long processId, Integer nextState) {
		Map paramMap = new HashMap();
		paramMap.put("processId", processId);
		int recNum = rdbSvc.exeSql(updateCurrStateSql, paramMap);

		if (recNum != 1)
			// throw new IFException("变更流程状态失败！！！");
			System.out.println("流程处理的Id不能为空！！！");

		BizState state = new BizState();
		state.setCurrFlag(true);
		state.setRunningFlag(false);
		state.setProcessId(processId);
		state.setStateDesc("");
		state.setStateTime(new Date(System.currentTimeMillis()));
		state.setStateType(false);
		state.setStatus(nextState);

		BizState bizstate = (BizState) rdbSvc.save(state);
		return bizstate;

	}

	/**
	 * 根据功能名称和当前状态，可以唯一确定一条FlowView记录，注意条件中的preStatus=status
	 * 
	 * @param processId
	 * @param funName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public FlowView getUniqueFlowView(Long processId, String funName) {
		Map paramMap = new HashMap();
		paramMap.put("processId", processId);
		paramMap.put("funName", funName);
		FlowView result = (FlowView) rdbSvc.findObject(getCurrStateView,
				paramMap);
		return result;
	}

	/**
	 * 根据用户LoginId查询用户所有的应该执行的交易
	 * @param loginId
	 * @return
	 */
	public List<?> getUserTransByLoginId(Map params) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("loginId", Long.parseLong(params.get("LoginId").toString()));
		Page<?> result = (Page<?>) rdbSvc.findAll2Page(getUserAction, param);
		return result.getContent();
	}
	
	//根据用户的PartyId&BizTypeId查询用户所有的
	public List<?> getUserTransByPartyIdAndBizTypeId(Map params){
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("partyId", Long.parseLong(params.get("partyId").toString()));
		param.put("bizTypeId", Integer.parseInt(params.get("bizTypeId").toString()));
		Page<?> resut = (Page<?>) rdbSvc.findAll2Page(getUserTransByPartyIdAndBizTypeId, param);
		return resut.getContent();
	}

	/**
	 * 获取所有的流程类型
	 * 
	 * @return
	 */
	public List<?> getAllTransType() {
		Map<String, Object> params = new HashMap<String, Object>();
		return rdbSvc.findList(getAllTransType, params);
	}

	/**
	 * 添加交易实体类
	 * @param maps
	 * @return
	 */
	public Object saveAllBusinessProcessMater(BizBusinessFlowBean FlowBean) {

		BizBusinessFlowContext bizContext = new BizBusinessFlowContext();
		Map<String, Object> entityValue = new HashMap<String, Object>();

		bizContext.setBizTypeId(FlowBean.getBizTypeId());
		bizContext.setProcessId(FlowBean.getProcessId());
		entityValue.put("List", FlowBean);
		bizContext.setBusinessflowContext(Util.toJsonString(entityValue)
				.toString());
		return rdbSvc.save(bizContext);
	}

	/**
	 * 根据BizType,processId
	 * 
	 * @param params
	 * @return
	 */
	public List<?> queryProcessMater(Map params) {
		List<?> result = rdbSvc.findList("from BizBusinessFlowContext bf where bf.bizTypeId=:bizType and bf.processId=:processId", params);
		return result;
	}

}
