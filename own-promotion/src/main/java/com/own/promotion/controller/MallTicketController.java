package com.own.promotion.controller;

import com.own.face.util.base.BaseController;
import com.own.promotion.dao.MallTicketDao;
import com.own.promotion.dao.domain.MallTicket;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value="/sale/mallTicket")
public class MallTicketController extends BaseController{

	@Autowired
	private MallTicketDao mallTicketDao;
	@ApiOperation(value = "查询单条商城券信息")
	@GetMapping("/{id}")
	public @ResponseBody MallTicket findMallTicketById(@PathVariable Integer id){
		log.info("查询id为："+id+"的范围");
		return mallTicketDao.getFromId(id);
	}

	@ApiOperation(value = "查询商城券列表")
	@GetMapping("/query/all")
	public @ResponseBody List<MallTicket> findAll(){
		List<MallTicket> list = new ArrayList<MallTicket>();
		list = mallTicketDao.findAllMallTicket();
		return list;
	}

	@ApiOperation(value = "保存商城券信息")
	@PostMapping("/save/scope")
	public @ResponseBody MallTicket save(@RequestBody MallTicket p){
		mallTicketDao.save(p);//创建节点
		mallTicketDao.createRelationship(p.getId().intValue(), 25, "DATA");//建立关系，24为模板节点的id,DATA为关系名
		return p;
	}

	@ApiOperation(value = "删除商城券数据以及关系")
	@DeleteMapping("/{id}")
	public @ResponseBody String deleteMallTicket(@PathVariable Integer id){
		log.info("删除节点id为："+id+"的数据");
		Object o = mallTicketDao.deleteRelationships(id);//删除该数据，并且删除关系
		return "123";
	}

	@ApiOperation(value = "修改商城券信息")
	@PutMapping("/update")
	public @ResponseBody MallTicket upMallTicket(MallTicket p){
		mallTicketDao.save(p);
		return p;
	}
}
