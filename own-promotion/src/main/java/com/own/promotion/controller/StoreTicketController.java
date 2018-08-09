package com.own.promotion.controller;

import java.util.ArrayList;
import java.util.List;

import com.own.face.util.base.BaseController;
import com.own.promotion.dao.StoreTicketDao;
import com.own.promotion.dao.domain.StoreTicket;
import io.swagger.annotations.ApiOperation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value="/sale/storeTicket")
public class StoreTicketController extends BaseController {
	@Autowired
	private StoreTicketDao storeTicketDao;
	
	Logger log = Logger.getLogger(StoreTicketController.class);
	

	@ApiOperation(value = "查询单条商城券信息")
	@GetMapping("/{id}")
	public @ResponseBody StoreTicket findStoreTicketById(@PathVariable Integer id){
		log.info("查询id为："+id+"的范围");
		return storeTicketDao.getFromId(id);
	}

	@ApiOperation(value = "查询商城券列表")
	@GetMapping("/query/all")
	public @ResponseBody List<StoreTicket> findAll(){
		List<StoreTicket> list = new ArrayList<StoreTicket>();
		list = storeTicketDao.findAllStoreTicket();
		return list;
	}

	@ApiOperation(value = "保存商城券信息")
	@PostMapping("/save/ticket")
	public @ResponseBody StoreTicket save(@RequestBody StoreTicket p){
		storeTicketDao.save(p);//创建节点
		storeTicketDao.createRelationship(p.getId().intValue(), 34, "DATA");//建立关系，24为模板节点的id,DATA为关系名
		return p;
	}
	

	@ApiOperation(value = "删除商城券数据以及关系")
	@DeleteMapping("/{id}")
	public @ResponseBody boolean deleteStoreTicket(@PathVariable Integer id){
		log.info("删除节点id为："+id+"的数据");
		Object o = storeTicketDao.deleteRelationships(id);//删除该数据，并且删除关系
		return true;
	}

	@ApiOperation(value = "修改商城券信息")
	@PutMapping("/update")
	public @ResponseBody StoreTicket upStoreTicket(StoreTicket p){
		storeTicketDao.save(p);
		return p;
	}
}
