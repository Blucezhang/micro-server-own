package workflow.action;


import java.util.HashMap;
import java.util.List;
import java.util.Map;














import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.governator.annotations.binding.Request;

import workflow.action.bean.BizBusinessFlowBean;
import workflow.domain.BizBusinessFlowContext;
import workflow.domain.BizState;
import workflow.service.FlowSvc;
  

/**
 * 
 * @author Administrator
 *
 */
@RestController
public class FlowAction  {
	
	@Autowired
	private FlowSvc flowSvc;
	
	/**
	 * 根据LoginId查询用户所有的交易
	 * @param params
	 * @return
	 */
	@RequestMapping(value="/workflow",method=RequestMethod.GET)
	public Map<String,String> getUserWorkFlowTransByLoginId(@RequestParam Map params){
		System.out.println(params.get("LoginId")+"LoginId");
		Map<String, String> result = new HashMap<String,String>();
		List<?> transList = flowSvc.getUserTransByLoginId(params);
		result.put("TransByLoginId", transList.toString());
		return result;
	}
	
	/**
	 * 根据PartyId&bizTypeId查询用户所有的交易
	 * @param params
	 * @return
	 */
	@RequestMapping(value="/workflow",method=RequestMethod.GET,params="ActionName=PartyIdAndBizTypeId")
	public Map<String,List<?>> getUserWorkFlowTransByPartIdAndBizType(@RequestParam Map params){
		Map<String,List<?>> result = new HashMap<String,List<?>>();
		List<?> transList =flowSvc.getUserTransByPartyIdAndBizTypeId(params);
		result.put("TransByPartyIdAndBizType", transList);
		return result;
	}
	
	/**
	 * 根据ProcessId跟BizType当前工作流的详细信息
	 * @param processId
	 * @return
	 */
	@RequestMapping(value="/workflow/{processId}/{bizType}",method=RequestMethod.GET)
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
	
	/**
	 * 查询工作流的所有类型
	 * @return
	 */
	@RequestMapping(value="/workflow",method=RequestMethod.GET,params="ActionName=FlowType")
	public Map<String,List<?>> getAllFlowType(){
		Map<String,List<?>> result = new HashMap<String,List<?>>();
		List<?> list = flowSvc.getAllTransType();
		result.put("transType", list);
		return result;
	}
	
	
	/**
	 * 记录同类型下交易内容详细
	 * @param params
	 * @return
	 */
	@RequestMapping(value="/workflow",method=RequestMethod.PUT)
	public Object putWorkFlowTrans(@RequestBody BizBusinessFlowBean FlowBean){
		BizBusinessFlowContext bContext = (BizBusinessFlowContext) flowSvc.saveAllBusinessProcessMater(FlowBean);
		return bContext;
	}
	
	/**
	 * 工作流驱动(根据工作流的当前状态查询出下一个状态，设置下一个工作流的状态)
	 * @param params
	 * @return
	 */
	@RequestMapping(value="/workflow",method=RequestMethod.POST)
	public Map<String,Object> updataWorkFlowTrans(@RequestBody Map params){
		Map<String,Object> result = new HashMap<String,Object>();
		BizState bizstate = (BizState) flowSvc.doFlow(params)	 ;
		result.put("NextState", bizstate);
		return result;
	}
	
	
	
	
}
