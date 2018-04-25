package com.own.promotion.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.promotion.dao.ProductDao;
import com.own.promotion.dao.domain.Product;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/sale/product")
public class ProductController {
	
	@Autowired
	private ProductDao productDao;
	
	Logger log = Logger.getLogger(ProductController.class);
	
	/**
	 * 查询单条scope信息
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/{id}",method={RequestMethod.GET})
	public Product findProductById(@PathVariable Integer id){
		log.info("查询id为："+id+"的范围");
		return productDao.getFromId(id);
	}
	
	/**
	 * 查询scope列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.GET})
	public List<Product> findAll(){
		List<Product> list = new ArrayList<Product>();
		list = productDao.findAllProduct();
		return list;
	}
	
	/**
	 * 保存scope信息
	 * @param p
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.POST})
	public Product save(@RequestBody Product p){
		
		productDao.save(p);//创建节点
		productDao.createRelationshipJoin(p.getId().intValue(), 7, "DATA");//建立关系，24为模板节点的id,DATA为关系名,此处关联活动的节点
		
		Map<String, Object> params = new HashMap<String, Object>(); 
		params.put( "s", 1 ); 
		params.put( "l", 1 ); 
		
		
		
		return p;
	}
	
	/**
	 * 删除scope数据以及关系
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/{id}",method={RequestMethod.DELETE})
	public String deleteProduct(@PathVariable Integer id){
		log.info("删除节点id为："+id+"的数据");
		Object o = productDao.deleteRelationships(id);//删除该数据，并且删除关系
		return "123";
	}
	
	/**
	 * 修改scope信息
	 * @param p
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.PUT})
	public Product upProduct(Product p){
		productDao.save(p);
		return p;
	}
	
	
	
}
