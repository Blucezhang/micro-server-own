package com.example.demo.controller;


import com.example.demo.dao.XxlJobGroupDao;
import com.example.demo.entity.XxlJobGroup;
import com.example.demo.server.XxlJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * job group controller
 * @author xuxueli 2016-10-02 20:52:56
 */
@Controller
@RequestMapping("/jobgroup")
public class JobGroupController {

	@Autowired
	public XxlJobService xxlJobService;

	@GetMapping
	public @ResponseBody
	List<XxlJobGroup> index() {
		// job group (executor)
		//List<XxlJobGroup> list = xxlJobService.findAll();
		int flag = xxlJobService.findAllCount();
		System.out.print(flag+"======================================");
		return new ArrayList<>();
	}

}
