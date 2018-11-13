package com.own.owncommon.order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.owncommon.core.FaceBase;
import com.own.owncommon.core.IfException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



@Slf4j
@Service
public class OrderFace extends FaceBase {

	protected String serviceUrl="//ORDER/";
	/**
	 * 订单查询
	 * @param id
	 * @return
	 */
	public OrderBean getOrder(String id){
		 OrderBean o = get (serviceUrl+ "/Order/{id}", OrderBean.class, id);
		 log.info("OrderId "+o.getOrderId()+"OrderNo "+o.getOrderNo()+"CreateDate "+o.getCreateDate());
		 return o;
	}
	
	/**
	 * BG订单明细
	 */
	
	public List getOrderDetial(Map parms){
		log.info(parms.toString()+"入参");
		List ss = get (serviceUrl+ "/Order?orderDetailId={orderDetailId}", List.class, parms);
		return ss;
	}
	
	/**
	 * 订单查询 - 根据订单状态
	 * @param state
	 * @return
	 */
	public void getOrderByState(int state){
		Map map = new HashMap();
		map.put("orderDetailState", state);
		Map returnMap = get(serviceUrl+ "/Order?orderDetailState={orderDetailState}", Map.class, map);
		log.info("resutl:{}"+returnMap.get("result"));
	}
	
	/**
	 * 订单查询 - 根据订单状态  和供应商partyId
	 * @param parms
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
		Map returnMap =get(serviceUrl+ "/Order/{id}/Buyer?productId={productId}&partyId={partyId}", Map.class, map);
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
		Map returnMap = get(serviceUrl+ "/Order/{id}/Seller?productId={productId}&partyId={partyId}", Map.class, map);
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
 	public OrderBean updOrder(OrderDetailBean od) throws IfException {
        Map<String,Object> map = new HashMap<String,Object>();
 		map.put("id", od.getOrderId());
 		OrderBean returnbean = post (serviceUrl+"/Order/{id}",od,OrderBean.class, map);
 		return returnbean;
 	}
	
}
