package com.own.promotion.controller;

import java.util.ArrayList;
import java.util.List;

import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.promotion.dao.StoreTicketDao;
import com.own.promotion.dao.domain.StoreTicket;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value="/sale/storeTicket")
public class StoreTicketController extends BaseController {
	@Autowired
	private StoreTicketDao storeTicketDao;
	
	@Operation(summary = "查询单条商城券信息")
	@GetMapping("/{id}")
	public @ResponseBody
	Resp findStoreTicketById(@PathVariable Integer id){
		log.info("查询id为："+id+"的范围");
		return new Resp(storeTicketDao.getFromId(id));
	}

	@Operation(summary = "查询商城券列表")
	@GetMapping("/query/all")
	public @ResponseBody Resp findAll(){
		return new Resp(storeTicketDao.findAllStoreTicket());
	}

	@Operation(summary = "保存商城券信息")
	@PostMapping("/save/ticket")
	public @ResponseBody Resp save(@RequestBody StoreTicket p){
		storeTicketDao.save(p);//创建节点
		storeTicketDao.createDataRelationship(p.getId().intValue(), 34);//建立 DATA 关系
		return new Resp(p);
	}
	

	@Operation(summary = "删除商城券数据以及关系")
	@DeleteMapping("/{id}")
	public @ResponseBody Resp deleteStoreTicket(@PathVariable Integer id){
		log.info("删除节点id为："+id+"的数据");
		//删除该数据，并且删除关系
		storeTicketDao.deleteRelationships(id);
		return new Resp(id);
	}

	@Operation(summary = "修改商城券信息")
	@PutMapping("/update")
	public @ResponseBody Resp upStoreTicket(StoreTicket p){
		return new Resp(storeTicketDao.save(p));
	}
}
