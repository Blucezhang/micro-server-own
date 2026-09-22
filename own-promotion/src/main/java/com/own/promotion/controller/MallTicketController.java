package com.own.promotion.controller;

import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.promotion.dao.MallTicketDao;
import com.own.promotion.dao.domain.MallTicket;
import io.swagger.v3.oas.annotations.Operation;
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
	@Operation(summary = "查询单条商城券信息")
	@GetMapping("/{id}")
	public @ResponseBody
	Resp findMallTicketById(@PathVariable Integer id){
		log.info("查询id为："+id+"的范围");
		return new Resp(mallTicketDao.getFromId(id));
	}

	@Operation(summary = "查询商城券列表")
	@GetMapping("/query/all")
	public @ResponseBody Resp findAll(){
		log.info("查询商城券列表");
		return new Resp(mallTicketDao.findAllMallTicket());
	}

	@Operation(summary = "保存商城券信息")
	@PostMapping("/save/scope")
	public @ResponseBody Resp save(@RequestBody MallTicket p){
		mallTicketDao.save(p);//创建节点
		mallTicketDao.createDataRelationship(p.getId().intValue(), 25);//建立 DATA 关系
		return new Resp(p);
	}

	@Operation(summary = "删除商城券数据以及关系")
	@DeleteMapping("/{id}")
	public @ResponseBody Resp deleteMallTicket(@PathVariable Integer id){
		log.info("删除节点id为："+id+"的数据");
		//删除该数据，并且删除关系
		mallTicketDao.deleteRelationships(id);
		return new Resp(id);
	}

	@Operation(summary = "修改商城券信息")
	@PutMapping("/update")
	public @ResponseBody Resp upMallTicket(MallTicket p){
		log.info("修改商城券信息");
		return new Resp(mallTicketDao.save(p));
	}
}
