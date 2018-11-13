package com.example.demo.server;

import com.example.demo.entity.XxlJobGroup;

import java.util.List;

/**
 * core job action for xxl-job
 * 
 * @author xuxueli 2016-5-28 15:30:33
 */
public interface XxlJobService {

	public List<XxlJobGroup> findAll();

	int findAllCount();

}
