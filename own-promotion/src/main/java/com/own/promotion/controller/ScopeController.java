package com.own.promotion.controller;

import java.util.ArrayList;
import java.util.List;

import com.own.promotion.dao.ScopeDao;
import com.own.promotion.dao.domain.Scope;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value="/sale/scope")
public class ScopeController {
	
	@Autowired
	private ScopeDao scopeDao;
	Logger log = Logger.getLogger(ScopeController.class);

	@ApiOperation(value = "查询单条scope信息")
	@GetMapping("/{id}")
	public @ResponseBody Scope findScopeById(@PathVariable Integer id){
		log.info("查询id为："+id+"的范围");
		return scopeDao.getFromId(id);
	}
	

	@ApiOperation(value = "查询scope列表")
	@GetMapping("/query/all")
	public @ResponseBody List<Scope> findAll(){
		List<Scope> list = new ArrayList<Scope>();
		list = scopeDao.findAllScope();
		return list;
	}
	

	@ApiOperation(value = "保存scope信息")
	@PostMapping("/save")
	public @ResponseBody Scope save(@RequestBody Scope p){
		Scope scope = scopeDao.save(p);//创建节点
		scopeDao.createRelationship(scope.getId().intValue(), 5, "DATA");//建立关系，24为模板节点的id,DATA为关系名
		return p;
	}

	@ApiOperation(value = "删除scope数据以及关系")
	@DeleteMapping("/{id}")
	public @ResponseBody boolean deleteScope(@PathVariable Integer id){
		log.info("删除节点id为："+id+"的数据");
		Object o = scopeDao.deleteRelationships(id);//删除该数据，并且删除关系
		return true;
	}

	@ApiOperation(value = "修改scope信息")
	@PutMapping("/update")
	public @ResponseBody Scope upScope(Scope p){
		scopeDao.save(p);
		return p;
	}
	
	
}
