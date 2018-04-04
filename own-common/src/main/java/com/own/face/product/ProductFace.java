package com.own.face.product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.face.core.FaceBase;
import com.own.face.core.IfException;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


@Service
public class ProductFace extends FaceBase {

 	protected String serviceUrl="//PRODUCT/";
 	
 	/**
 	 * 查询产品
 	 * @param id
 	 * @throws IfException
 	 */
 	public Map getProductById(Long id) throws IfException {
 		
 		Map<String,Object> map = new HashMap<String,Object>();
 		map.put("id", id);
 		ProductBean p =  get (serviceUrl+"/Product/{id}", ProductBean.class, map);
 		Map<String,Object> productObjMap = new HashMap<String,Object>();
 		productObjMap.put("result", p);
 		
 		return productObjMap;
 	}
 	 
 	/**
 	 * 查询产品
 	 * @param categoryIds 需要查询的类别
 	 * @throws IfException
 	 */
 	public List getProduct(String categoryIds,String partyId) throws IfException{
 		System.out.println("开始...................... "+categoryIds);
 		Map<String,Object> map = new HashMap<String,Object>();
 		map.put("categoryId", categoryIds);
 		if(partyId!=null&&partyId!=""){
 		map.put("partyId", partyId);
 		}else{
 			map.put("partyId", null);
 		}
 		List list=null;
 		if(categoryIds!=null&&categoryIds!=""){
 		 list =  (List) restTemplate.getForObject(serviceUrl+"/Product?categoryId={categoryId}&partyId={partyId}", List.class, map);
 		}else{
 			 list =  (List) restTemplate.getForObject(serviceUrl+"/Product", List.class, map);
 		}
 		return list;
 	}
 	
 	
 	/**
 	 * 查询产品
 	 * @param productIds 需要查询的类别
 	 * @throws IfException
 	 */
 	public List getProductByIds(String productIds) throws IfException{
 		System.out.println("开始...................... "+productIds);
 		Map<String,Object> map = new HashMap<String,Object>();
 		map.put("productId", productIds);
 		List list =restTemplate.getForObject(serviceUrl+"/Product?productId={productId}", List.class, map);
 		return list;
 	}
 	
 	/**
 	 * 查询产品
 	 * @param partyId 
 	 * @throws IfException
 	 */
 	public List getProductByPartyId(String partyId) throws IfException{

 		Map<String,Object> map = new HashMap<String,Object>();
 		map.put("partyId", partyId);
 		List result =   get (serviceUrl+"/Product?partyId={partyId}", List.class, map);
 		
 		return result;
 	}
 	
 	/**
 	 * 查询类别
 	 * @param level 类别等级
 	 * @throws IfException
 	 */
 	public List<CategoryBean> getCategory(String level) throws IfException{

 		Map<String,Object> map = new HashMap<String, Object>();
 		map.put("level", level);
 		List<CategoryBean> returnList = get (serviceUrl+"/Category?level={level}", List.class, map);
 		return returnList;
 	}
 	
 	/**
 	 * 添加类别
 	 * @param c
 	 * @throws IfException
 	 */
 	public void addCategory(CategoryBean c) throws IfException{
 		System.out.println("ProductFace : "+c.getName());
 		restTemplate.put(serviceUrl+"/Category", c);
 	}
 	
 	/**
 	 * 添加产品
 	 * @param p
 	 * @throws IfException
 	 */
 	public ProductBean addProduct(ProductBean p) throws IfException{

 		ProductBean pb = put(serviceUrl+"/Product", p, ProductBean.class);
 		
 		return pb;
 	}
 	
 	/**
 	 * 修改产品
 	 * @param p
 	 * @throws IfException
 	 */
 	public Map updProduct(ProductBean p) throws IfException{
 		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json");
   
        HttpEntity request = new HttpEntity(p, headers);
        Map<String,Object> map = new HashMap<String,Object>();
 		map.put("id", p.getId());
 		Map returnMap = post(serviceUrl+"/Product/{id}",request,Map.class, map);
 		return returnMap;
 	}
	  
    /***** Template *****/
 	
 	/**
 	 * 查询产品模板
 	 * @param id
 	 * @throws IfException
 	 */
 	public Map getTemplateById(Long id) throws IfException{
 		Map<String,Object> map = new HashMap<String,Object>();
 		map.put("id", id);
 		TemplateBean t =  get (serviceUrl+"/Template/{id}", TemplateBean.class, map);
 		Map<String,Object> templateObjMap = new HashMap<String,Object>();
 		templateObjMap.put("result", t);
 		return templateObjMap;
 	}
 	
 	/**
 	 * 查询产品模版-列表
 	 */
 	public List queryTemplateList(Map<String,Object> parms) throws IfException {
 		System.out.println("face----------run"+parms);
 
 		List result = get (serviceUrl+"/Template", List.class, parms);
 		return result;
 	}
 	
 	/**
 	 * 添加产品模板
 	 * @param t
 	 * @throws IfException
 	 */
 	public TemplateBean addTemplate(TemplateBean t) throws IfException{
 		TemplateBean tb = put(serviceUrl+"/Template", t, TemplateBean.class);
 		System.out.println(tb.getId());
 		return tb;
 	}
 	
 	/**
 	 * 修改产品模板
 	 * @param t
 	 * @throws IfException
 	 */
 	public TemplateBean updTemplate(TemplateBean t) throws IfException{
 		Map map = new HashMap();
 		map.put("id", t.getId());
 		TemplateBean tb = post(serviceUrl+"/Template/{id}", t, TemplateBean.class,map);
 		System.out.println(tb.getName());
 		return tb;
 	}
	
}
