package com.own.promotion.controller;

import com.own.face.util.Resp;
import com.own.promotion.dao.ScopeDao;
import com.own.promotion.dao.domain.Scope;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value="/sale/scope")
public class ScopeController {
	
	@Autowired
	private ScopeDao scopeDao;

	@ApiOperation(value = "查询单条scope信息")
	@GetMapping("/{id}")
	public @ResponseBody Resp findScopeById(@PathVariable Integer id){
		log.info("查询id为："+id+"的范围");
		return new Resp(scopeDao.getFromId(id));
	}
	

	@ApiOperation(value = "查询scope列表")
	@GetMapping("/query/all")
	public @ResponseBody Resp findAll(){
		return new Resp(scopeDao.findAllScope());
	}
	

	@ApiOperation(value = "保存scope信息")
	@PostMapping("/save")
	public @ResponseBody Resp save(@RequestBody Scope p){
		Scope scope = scopeDao.save(p);//创建节点
		scopeDao.createRelationship(scope.getId().intValue(), 5, "DATA");//建立关系，24为模板节点的id,DATA为关系名
		return new Resp(p);
	}

	@ApiOperation(value = "删除scope数据以及关系")
	@DeleteMapping("/{id}")
	public @ResponseBody Resp deleteScope(@PathVariable Integer id){
		log.info("删除节点id为："+id+"的数据");
		//删除该数据，并且删除关系
		return new Resp(scopeDao.deleteRelationships(id));
	}

	@ApiOperation(value = "修改scope信息")
	@PutMapping("/update")
	public @ResponseBody
	Resp upScope(Scope p){
		return new Resp(scopeDao.save(p));
	}
	
	
}
