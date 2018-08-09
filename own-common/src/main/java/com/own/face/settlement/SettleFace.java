package com.own.face.settlement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.face.core.FaceBase;
import com.own.face.core.IfException;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


public class SettleFace extends FaceBase {


 	protected String serviceUrl="//SETTLEMENT/";

	protected org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this.getClass());
      
 	/**
 	 * 支付交易
 	 * @param s
 	 * @throws IfException
 	 */
 	public SettlementBean addSettlement(SettlementBean s) throws IfException {
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
        String requestBody = "{\"acctId\":\""+ab.getAcctId()+ "\",\"amount\":\"" +ab.getAmount()+"\"}";
        Map<String,Object> map = new HashMap<String,Object>();
 		map.put("acctId", ab.getAcctId());
 		map.put("Action", "Buyer");
		AcctBean returnbean =  post(serviceUrl+"/Account/{acctId}?Action={Action}",requestBody,AcctBean.class,map);
 		return returnbean;
 	}
    
}
 