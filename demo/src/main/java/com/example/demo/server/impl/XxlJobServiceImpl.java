package com.example.demo.server.impl;


import com.example.demo.dao.XxlJobGroupDao;
import com.example.demo.dao.XxlJobInfoDao;
import com.example.demo.entity.XxlJobGroup;
import com.example.demo.server.XxlJobService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * core job action for xxl-job
 * @author xuxueli 2016-5-28 15:30:33
 */
@Service
public class XxlJobServiceImpl implements XxlJobService {
	private static Logger logger = LoggerFactory.getLogger(XxlJobServiceImpl.class);

	@Autowired
	private XxlJobGroupDao xxlJobGroupDao;

	@Autowired
	private XxlJobInfoDao xxlJobInfoDao;


	@Override
	public List<XxlJobGroup> findAll() {
		return xxlJobGroupDao.queryAll();
	}

	@Override
	public int findAllCount() {
		return xxlJobInfoDao.findAllCount();
	}
}
