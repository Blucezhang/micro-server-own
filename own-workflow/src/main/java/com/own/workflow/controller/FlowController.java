package com.own.workflow.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.workflow.controller.bean.BizBusinessFlowBean;
import com.own.workflow.domain.BizBusinessFlowContext;
import com.own.workflow.domain.BizState;
import com.own.workflow.service.FlowSvc;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author BluceZhang
 */
@RestController
@RequestMapping("/flow")
public class FlowController{
	
	@Autowired
	private FlowSvc flowSvc;

	@ApiOperation(value = "根据LoginId查询用户所有的交易")
	@GetMapping("/workflow")
	public Map<String,String> getUserWorkFlowTransByLoginId(@RequestParam Map params){
		System.out.println(params.get("LoginId")+"LoginId");
		Map<String, String> result = new HashMap<String,String>();
		List<?> transList = flowSvc.getUserTransByLoginId(params);
		result.put("TransByLoginId", transList.toString());
		return result;
	}

	@ApiOperation(value = "根据PartyId&bizTypeId查询用户所有的交易")
	@GetMapping(value = "/workflow",params="ActionName=PartyIdAndBizTypeId")
	public Map<String,List<?>> getUserWorkFlowTransByPartIdAndBizType(@RequestParam Map params){
		Map<String,List<?>> result = new HashMap<String,List<?>>();
		List<?> transList =flowSvc.getUserTransByPartyIdAndBizTypeId(params);
		result.put("TransByPartyIdAndBizType", transList);
		return result;
	}
	

	@ApiOperation(value = "根据ProcessId跟BizType当前工作流的详细信息")
	@GetMapping("/workflow/{processId}/{bizType}")
	public Map<String,List<?>> getTransInfor(@PathVariable Long processId,@PathVariable Integer bizType){
		Map<String,List<?>> result = new HashMap<String,List<?>>();
		Map<String,Object> params = new HashMap<String,Object>();
		if(processId==null)
			System.out.println("processId不能为空");
		params.put("bizType", bizType);
		if(bizType==null)
			System.out.println("bizType不能为空");
		params.put("processId", processId);
		List<?> returns = flowSvc.queryProcessMater(params);
		result.put("TransListByProcessId", returns);
		return result;
	}

	@ApiOperation(value = "查询工作流的所有类型")
	@GetMapping(value="/workflow",params="ActionName=FlowType")
	public Map<String,List<?>> getAllFlowType(){
		Map<String,List<?>> result = new HashMap<String,List<?>>();
		List<?> list = flowSvc.getAllTransType();
		result.put("transType", list);
		return result;
	}

	@ApiOperation(value = "记录同类型下交易内容详细")
	@PutMapping(value="/workflow")
	public Object putWorkFlowTrans(@RequestBody BizBusinessFlowBean FlowBean){
		BizBusinessFlowContext bContext = (BizBusinessFlowContext) flowSvc.saveAllBusinessProcessMater(FlowBean);
		return bContext;
	}

	@ApiOperation(value = "工作流驱动(根据工作流的当前状态查询出下一个状态，设置下一个工作流的状态)")
	@PostMapping(value="/workflow")
	public Map<String,Object> updataWorkFlowTrans(@RequestBody Map params){
		Map<String,Object> result = new HashMap<String,Object>();
		BizState bizstate = (BizState) flowSvc.doFlow(params);
		result.put("NextState", bizstate);
		return result;
	}
}
