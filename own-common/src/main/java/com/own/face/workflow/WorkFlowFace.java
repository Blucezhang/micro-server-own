package com.own.face.workflow;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.face.core.FaceBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkFlowFace extends FaceBase {

	private String ServiceUrl = "http://WORKFLOW/";

	/**
	 * getAllWorkFlowByLoginId
	 * @param id
	 * @return
	 */
	public Map<String,Object> getAllWorkFlowByLoginId(Long id){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("LoginId", id);
		Map<String,Object> maps = get(ServiceUrl+"/workflow?LoginId={LoginId}", Map.class, params);
		log.info(maps.get("TransByLoginId")+"TransByLoginId");
		return maps;
	}


	/**
	 * getUserWorkFlowTransByPartIdAndBizType
	 * @param partId
	 * @param bizTypeId
	 * @return
	 */
	public Map<String,List<?>> getUserWorkFlowTransByPartIdAndBizType(Long partId,Integer bizTypeId){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("partyId", partId);
		params.put("bizTypeId", bizTypeId);
		params.put("ActionName", "PartyIdAndBizTypeId");
		Map<String,List<?>> maps = get(ServiceUrl+"/workflow?partyId={partyId}&bizTypeId={bizTypeId}&ActionName={ActionName}", Map.class, params);
		return maps;
	}

	/**
	 * 根据工作流ID以及工作流类型查询工作流的详细
	 * @param processId
	 * @param bizType
	 * @return
	 */
	public Map<String,List<?>> getTransInfo(Long processId,Long bizType){
		Map params = new HashMap();
		params.put("processId",processId);
		params.put("bizType",bizType);
		Map<String,List<?>> maps = get(ServiceUrl+"/workflow/{processId}/{bizType}", Map.class,params);
		return maps;
	}


	/**
	 * 获取所有的工作流类型
	 * @return
	 */
	public List getAllType(){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("ActionName", "FlowType");
		List obj=restTemplate.getForObject(ServiceUrl+"/workflow?ActionName={ActionName}", List.class, params);
		return obj;
	}


	/**
	 * 更新一个工作流
	 * @param processId
	 * @param funName
	 * @return
	 */
	public Map<String,Object> updataFlowTrans(Long processId,String funName){
        String requestBody = "{\"processId\":\""+processId+"\",\"funName\":\"" + funName + "\"}";
		Map<String,Object> obj = restTemplate.postForObject(ServiceUrl+"/workflow", requestBody, Map.class, "null");
		return obj;
	}

	/**
	 * 创建一个工作流
	 * @param maps
	 * @return
	 */
	public Object putWorkFlow(Map maps){
		 restTemplate.put(ServiceUrl+"/workflow", maps);
		return null;
	}
	
}
