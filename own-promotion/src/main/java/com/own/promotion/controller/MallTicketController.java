package com.own.promotion.controller;

import com.own.face.util.Resp;
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
	public @ResponseBody
	Resp findMallTicketById(@PathVariable Integer id){
		log.info("查询id为："+id+"的范围");
		return new Resp(mallTicketDao.getFromId(id));
	}

	@ApiOperation(value = "查询商城券列表")
	@GetMapping("/query/all")
	public @ResponseBody Resp findAll(){
		log.info("查询商城券列表");
		return new Resp(mallTicketDao.findAllMallTicket());
	}

	@ApiOperation(value = "保存商城券信息")
	@PostMapping("/save/scope")
	public @ResponseBody Resp save(@RequestBody MallTicket p){
		mallTicketDao.save(p);//创建节点
		mallTicketDao.createRelationship(p.getId().intValue(), 25, "DATA");//建立关系，24为模板节点的id,DATA为关系名
		return new Resp(p);
	}

	@ApiOperation(value = "删除商城券数据以及关系")
	@DeleteMapping("/{id}")
	public @ResponseBody Resp deleteMallTicket(@PathVariable Integer id){
		log.info("删除节点id为："+id+"的数据");
		//删除该数据，并且删除关系
		return new Resp(mallTicketDao.deleteRelationships(id));
	}

	@ApiOperation(value = "修改商城券信息")
	@PutMapping("/update")
	public @ResponseBody Resp upMallTicket(MallTicket p){
		log.info("修改商城券信息");
		return new Resp(mallTicketDao.save(p));
	}
}
