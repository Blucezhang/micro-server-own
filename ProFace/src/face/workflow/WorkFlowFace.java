package face.workflow;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import face.core.FaceBase;

@Service
public class WorkFlowFace extends FaceBase {
	
	@Autowired
	@LoadBalanced
	private RestTemplate restTemplate = null;
	
	private String ServiceUrl = "http://WORKFLOW/";
	
	public Map<String,Object> getAllWorkFlowByLoginId(Long id){
		Map<String,Object> params = new HashMap<String, Object>();
		params.put("LoginId", id);
		Map<String,Object> maps = restTemplate.getForObject(ServiceUrl+"/workflow?LoginId={LoginId}", Map.class, params);
		System.out.println(maps.get("TransByLoginId")+"TransByLoginId");
		return maps;
	}
	
	
	public Map<String,List<?>> getUserWorkFlowTransByPartIdAndBizType(Long partId,Integer bizTypeId){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("partyId", partId);
		params.put("bizTypeId", bizTypeId);
		params.put("ActionName", "PartyIdAndBizTypeId");
		Map<String,List<?>> maps = restTemplate.getForObject(ServiceUrl+"/workflow?partyId={partyId}&bizTypeId={bizTypeId}&ActionName={ActionName}", Map.class, params);
		return maps;
	}
	
	public Map<String,List<?>> getTransInfo(Long processId,Long bizType){
		Map<String,List<?>> maps = restTemplate.getForObject(ServiceUrl+"/workflow/{processId}/{bizType}", Map.class,processId,bizType);
		return maps;
	}
	
	
	public List getAllType(){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("ActionName", "FlowType");
		List obj=restTemplate.getForObject(ServiceUrl+"/workflow?ActionName={ActionName}", List.class, params);
		return obj;
	}
	
	
	public Map<String,Object> updataFlowTrans(Long processId,String funName){
		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");
        String requestBody = "{\"processId\":\""+processId+"\",\"funName\":\"" + funName + "\"}";
        HttpEntity request = new HttpEntity(requestBody, headers);

		Map<String,Object> obj = restTemplate.postForObject(ServiceUrl+"/workflow", request, Map.class, "null");
		return obj;
	}
	
	public Object putWorkFlow(Map maps){
		 restTemplate.put(ServiceUrl+"/workflow", maps);
		return null;
	}
	
}
