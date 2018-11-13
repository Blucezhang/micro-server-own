package com.own.owncommon.product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.owncommon.core.FaceBase;
import com.own.owncommon.core.IfException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;


@Slf4j
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
 		log.info("productObjMap{}:",productObjMap.toString());
 		return productObjMap;
 	}
 	 
 	/**
 	 * 查询产品
 	 * @param categoryIds 需要查询的类别
 	 * @throws IfException
 	 */
 	public List getProduct(String categoryIds,String partyId) throws IfException{
		List list= new ArrayList();
		Map<String,Object> map = new HashMap<String,Object>();
 		log.info("开始...{}",categoryIds);
 		map.put("categoryId", categoryIds);
 		if(partyId!=null&&partyId!=""){
 		map.put("partyId", partyId);
 		}else{
 			map.put("partyId", null);
 		}

 		if(categoryIds!=null&&categoryIds!=""){
 		 list =  get(serviceUrl+"/Product?categoryId={categoryId}&partyId={partyId}", List.class, map);
 		}else{
 			 list= get(serviceUrl+"/Product", List.class, map);
 		}
 		return list;
 	}
 	
 	
 	/**
 	 * 查询产品
 	 * @param productIds 需要查询的类别
 	 * @throws IfException
 	 */
 	public List getProductByIds(String productIds) throws IfException{
 		log.info("开始...{}",productIds);
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
 		log.info("ProductFace {} : ",c.getName());
 		restTemplate.put(serviceUrl+"/Category", c);
 	}
 	
 	/**
 	 * 添加产品
 	 * @param p
 	 * @throws IfException
 	 */
 	public ProductBean addProduct(ProductBean p) throws IfException{
 		log.info("object{};",p.toString());
 		ProductBean pb = put(serviceUrl+"/Product", p, ProductBean.class);
 		return pb;
 	}
 	
 	/**
 	 * 修改产品
 	 * @param p
 	 * @throws IfException
 	 */
 	public Map updProduct(ProductBean p) throws IfException{
        Map<String,Object> map = new HashMap<String,Object>();
 		map.put("id", p.getId());
 		Map returnMap = post(serviceUrl+"/Product/{id}",p,Map.class, map);
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
 		log.info("face{}：",parms);
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
 		log.info("return id {}",tb.getId());
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
 		log.info("tb Name {}",tb.getName());
 		return tb;
 	}
	
}
