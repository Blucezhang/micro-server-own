package com.own.promotion.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.face.util.Resp;
import com.own.face.util.base.BaseController;
import com.own.promotion.dao.ProductDao;
import com.own.promotion.dao.domain.Product;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value="/sale/product")
public class ProductController extends BaseController {
	
	@Autowired
	private ProductDao productDao;

	@ApiOperation(value = "查询单条scope信息")
	@GetMapping("/{id}")
	public @ResponseBody
	Resp findProductById(@PathVariable Integer id){
		log.info("查询id为："+id+"的范围");
		return new Resp(productDao.getFromId(id));
	}

	@ApiOperation(value = "查询scope列表")
	@GetMapping("/query/all")
	public @ResponseBody Resp findAll(){
		return new Resp(productDao.findAllProduct());
	}

	@ApiOperation(value = "保存scope信息",response =Product.class )
	@PostMapping("/save/scope")
	public @ResponseBody Resp save(@RequestBody Product p){
		productDao.save(p);//创建节点
		productDao.createRelationshipJoin(p.getId().intValue(), 7, "DATA");//建立关系，24为模板节点的id,DATA为关系名,此处关联活动的节点
		Map<String, Object> params = new HashMap<String, Object>(); 
		params.put( "s", 1 ); 
		params.put( "l", 1 );
		return new Resp(params);
	}

	@ApiOperation(value = "删除scope数据以及关系")
	@DeleteMapping("/{id}")
	public @ResponseBody Resp deleteProduct(@PathVariable Integer id){
		log.info("删除节点id为："+id+"的数据");
		//删除该数据，并且删除关系
		return new Resp(productDao.deleteRelationships(id));
	}

	@ApiOperation(value = "修改scope信息")
	@PutMapping("/update/scope")
	public @ResponseBody Resp upProduct(Product p){
		log.info("修改scope值",p.toString());
		return new Resp(productDao.save(p));
	}
	
}
