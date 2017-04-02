package face.order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import face.core.FaceBase;
import face.core.IfException;

public class OrderFace extends FaceBase{

	protected String serviceUrl="//ORDER/";
	
	/**
	 * 订单查询
	 * @param id
	 * @return
	 */
	public OrderBean getOrder(String id){
		 OrderBean o = restTemplate.getForObject (serviceUrl+ "/Order/{id}", OrderBean.class, id);
		 System.out.println("OrderId "+o.getOrderId());
		 System.out.println("OrderNo "+o.getOrderNo());
		 System.out.println("CreateDate "+o.getCreateDate());
		 return o;
	}
	
	/**
	 * BG订单明细
	 */
	
	public List getOrderDetial(Map parms){
	
		System.out.println(parms);
		List ss = get (serviceUrl+ "/Order?orderDetailId={orderDetailId}", List.class, parms);
		
		return ss;
	}
	
	/**
	 * 订单查询 - 根据订单状态
	 * @param id
	 * @return
	 */
	public void getOrderByState(int state){
		Map map = new HashMap();
		map.put("orderDetailState", state);
		Map returnMap = restTemplate.getForObject(serviceUrl+ "/Order?orderDetailState={orderDetailState}", Map.class, map);
		System.out.println(returnMap.get("result"));
	}
	
	/**
	 * 订单查询 - 根据订单状态  和供应商partyId
	 * @param id
	 * @return
	 */
	public List getOrderByStateAndPartyId(Map parms){
		
		List returnmap = get (serviceUrl+ "/Order?Action={Action}&orderDetailState={orderDetailState}&partyId={partyId}", List.class, parms);
		
		return returnmap;
	}
	
	/**
	 * 订单查询 - 买方
	 * @param id
	 * @return
	 */
	public Map getOrderBuyer(String id,String productId,String partyId){
		Map map = new HashMap();
		map.put("id",id);
		map.put("productId", productId);
		map.put("partyId", partyId);
		Map returnMap = restTemplate.getForObject(serviceUrl+ "/Order/{id}/Buyer?productId={productId}&partyId={partyId}", Map.class, map);
		return returnMap;
	}
	
	/**
	 * 订单查询 - 卖方
	 * @param id
	 * @return
	 */
	public Map getOrderSeller(Long id,Long productId,Long partyId){
		Map map = new HashMap();
		map.put("id",id);
		map.put("productId", productId);
		map.put("partyId", partyId);
		Map returnMap = restTemplate.getForObject(serviceUrl+ "/Order/{id}/Seller?productId={productId}&partyId={partyId}", Map.class, map);
		return returnMap;
	}
	
	/**
	 * 创建订单
	 * @param o
	 */
	public OrderBean addOrder(OrderBean o){
		return put(serviceUrl+ "/Order", o, OrderBean.class);
	}
	
	/**
 	 * 修改订单状态
 	 * @param od
 	 * @throws IfException
 	 */
 	public OrderBean updOrder(OrderDetailBean od) throws IfException{
 		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");      
        HttpEntity request = new HttpEntity(od, headers);             
        Map<String,Object> map = new HashMap<String,Object>();
 		map.put("id", od.getOrderId());
 		OrderBean returnbean = post (serviceUrl+"/Order/{id}",request,OrderBean.class, map);

 		return returnbean;
 	}
	
}
