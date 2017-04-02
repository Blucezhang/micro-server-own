package face.settlement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import face.core.FaceBase;
import face.core.IfException;

public class SettleFace extends FaceBase{

 	protected String serviceUrl="//SETTLEMENT/";
      
 	/**
 	 * 支付交易
 	 * @param c
 	 * @throws IfException
 	 */
 	public SettlementBean addSettlement(SettlementBean s) throws IfException{
 		return put(serviceUrl+"/Settlement", s,SettlementBean.class);
 	}
 	
 	/**
 	 * 绑定银行卡
 	 * @param a
 	 * @throws IfException
 	 */
 	public void addAccount(AcctBean a) throws IfException{

 		restTemplate.put(serviceUrl+"/Account", a);
 	}
 	
 	public List getAccount(String partyId ) throws IfException{
 		Map map=new HashMap();
 		map.put("partyId",partyId);
 		List returnlist = get (serviceUrl+"/Account?partyId={partyId}", List.class, map);
 		return returnlist;
 	}
 	public AcctBean PayToSeller(AcctBean ab) throws IfException{
 		
 		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");
        String requestBody = "{\"acctId\":\""+ab.getAcctId()+ "\",\"amount\":\"" +ab.getAmount()+"\"}";
        HttpEntity request = new HttpEntity(requestBody, headers);
        Map<String,Object> map = new HashMap<String,Object>();
 		map.put("acctId", ab.getAcctId());
 		map.put("Action", "Buyer");
 		AcctBean returnbean = restTemplate.postForObject(serviceUrl+"/Account/{acctId}?Action={Action}",request,AcctBean.class, map);
 		return returnbean;
 	}
    
}
 