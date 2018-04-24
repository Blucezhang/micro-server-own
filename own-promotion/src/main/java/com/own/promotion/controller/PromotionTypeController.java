package com.own.promotion.controller;

import java.util.ArrayList;
import java.util.List;

import com.own.promotion.dao.PromotionTypeDao;
import com.own.promotion.dao.domain.PromotionType;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/sale/promotionType")
public class PromotionTypeController {
	@Autowired
	private PromotionTypeDao promotionTypeDao;
	
	Logger log = Logger.getLogger(PromotionTypeController.class);
	
	/**
	 * 查询单条活动信息
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/{id}",method={RequestMethod.GET})
	public PromotionType findPromotionTypeById(@PathVariable Integer id){
		log.info("查询id为："+id+"的活动类型");
		return promotionTypeDao.getFromId(id);
	}
	
	/**
	 * 查询活动列表，支持搜索
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.GET})
	public List<PromotionType> findAll(){
		List<PromotionType> list = new ArrayList<PromotionType>();
		list = promotionTypeDao.findAllPromotionType();
		return list;
	}
	
	/**
	 * 保存活动信息
	 * @param p
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.POST})
	public PromotionType save(@RequestBody PromotionType p){
		
		promotionTypeDao.save(p);//创建节点
		promotionTypeDao.createRelationship(p.getId().intValue(), 24, "DATA");//建立关系，24为模板节点的id,DATA为关系名
		
		return p;
	}
	
	/**
	 * 删除数据以及关系
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/{id}",method={RequestMethod.DELETE})
	public String deletePromotion(@PathVariable Integer id){
		log.info("删除节点id为："+id+"的数据");
		Object o = promotionTypeDao.deleteRelationships(id);//删除该数据，并且删除关系
		return "123";
	}
	
	/**
	 * 修改信息
	 * @param p
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.PUT})
	public PromotionType upPromotion(PromotionType p){
//		promotionTypeDao.updatePromotion(id, map);
		promotionTypeDao.save(p);
		
		return p;
	}
}
