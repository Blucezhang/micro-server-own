package com.own.promotion.controller;

import java.util.ArrayList;
import java.util.List;

import com.own.promotion.dao.ScopeDao;
import com.own.promotion.dao.domain.Scope;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping(value="/sale/scope")
public class ScopeController {
	
	@Autowired
	private ScopeDao scopeDao;
	
	Logger log = Logger.getLogger(ScopeController.class);
	
	/**
	 * 查询单条scope信息
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/{id}",method={RequestMethod.GET})
	public Scope findScopeById(@PathVariable Integer id){
		log.info("查询id为："+id+"的范围");
		return scopeDao.getFromId(id);
	}
	
	/**
	 * 查询scope列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.GET})
	public List<Scope> findAll(){
		List<Scope> list = new ArrayList<Scope>();
		list = scopeDao.findAllScope();
		return list;
	}
	
	/**
	 * 保存scope信息
	 * @param p
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.POST})
	public Scope save(@RequestBody Scope p){
		
		Scope scope = scopeDao.save(p);//创建节点
		scopeDao.createRelationship(scope.getId().intValue(), 5, "DATA");//建立关系，24为模板节点的id,DATA为关系名
		
		return p;
	}
	
	/**
	 * 删除scope数据以及关系
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/{id}",method={RequestMethod.DELETE})
	public String deleteScope(@PathVariable Integer id){
		log.info("删除节点id为："+id+"的数据");
		Object o = scopeDao.deleteRelationships(id);//删除该数据，并且删除关系
		return "123";
	}
	
	/**
	 * 修改scope信息
	 * @param p
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.PUT})
	public Scope upScope(Scope p){
		scopeDao.save(p);
		return p;
	}
	
	
}
