package com.own.user.party.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.face.party.FunBean;
import com.own.face.util.base.BaseController;
import com.own.user.party.dao.FunDao;
import com.own.user.party.dao.domain.Fun;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

/**
 * 功能
 * @author Blucezhang
 *
 */
@Slf4j
@RestController
@RequestMapping("/Fun")
public class FunController extends BaseController {
	
	@Autowired
	private FunDao funDao = null;

	@ApiOperation(value = "添加功能")
	@PutMapping("/Fun")
	public @ResponseBody void createFunSet(@RequestBody FunBean funBean){
		Fun fun = new Fun();
		fun.setName(funBean.getName());
		fun.setNote(funBean.getNote());
		fun.setConfig(funBean.getConfig());
		fun.setDicConfig(funBean.getDicconfig());
		fun.setBizTypeId(funBean.getBiztypId());
		funDao.save(fun);
		funDao.createRelationShipOfFun(fun.getFunctionId());
	}

	@ApiOperation("查询所有的功能")
	@GetMapping("/Fun")
	public @ResponseBody List<Fun> getAllFun(){
		Map<String,Object> result = new HashMap<String, Object>();
		List<Fun> funList = funDao.queryAllFun();
		return funList;
	}

	@ApiOperation(value ="根据Id删除功能" )
	@DeleteMapping("/Fun/{id}")
	public @ResponseBody void deleteFunById(@PathVariable Long Id){
		funDao.deleteFun(Id);
	}
	

	@ApiOperation(value = "根据Id查询功能的详细信息")
	@GetMapping("/Fun/{functionId}")
	public Fun getInforbyId(@PathVariable Long functionId){
		Map<String,Object> result = new HashMap<String,Object>();
		Fun fun = funDao.queryOneFunbyId(functionId);
		return fun;
	}

	@ApiOperation(value = "根据Id修改功能信息")
	@PostMapping("/Fun/{id}")
	public @ResponseBody void updataFunById(@PathVariable Long id,@RequestBody FunBean funBean){
		Fun fun = funDao.getFromId(Integer.parseInt(id.toString()));
		fun.setName(funBean.getName());
		fun.setNote(funBean.getNote());
		fun.setDicConfig(funBean.getDicconfig());
		fun.setConfig(funBean.getConfig());
		fun.setBizTypeId(funBean.getBiztypId());
		funDao.save(fun);
	}
}
