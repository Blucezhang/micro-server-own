package com.own.send.server.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.send.server.domain.Sms;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class SmsSvc {
	
	Logger log = Logger.getLogger(SmsSvc.class);
	
	@Autowired
	private RdbSvc rdbSvc;
	
	//hql中注意实体名
	private String findSmsSql = " from Sms AS s where 1=1 ";
	
	/**
	 * 查询短信列表,搜索功能
	 * @return
	 */
	public List<?> getSms(String wheresql) {
		Map<String,Object> param = new HashMap<String,Object>();
	 
		Page<?> result = (Page<?>)rdbSvc.findAll2Page(findSmsSql+wheresql, param);
		return  result.getContent();
	}
	
	/**
	 * 保存数据
	 * @param sms
	 * @return
	 */
	public Sms save(Sms sms){
		try{
//			rdbSvc.exeSql("set names utf8mb4", null);//设置数据库编码
			sms = (Sms)rdbSvc.save(sms);
			
		}catch(Exception ee){
			log.error("短信保存失败!失败原因："+ee.getMessage());
			ee.printStackTrace();
		}
		
		return sms;
	}
	
	/**
	 * 根据id查询短信
	 * @param id
	 * @return
	 */
	public Sms findSmsById(Integer id){
		String findSmsHql = " from Sms s where s.id=:id";
		Map map = new HashMap();
		map.put("id", id);
		Sms sms = null;
		try{
			sms = (Sms)rdbSvc.findObject(findSmsHql, map);
		}catch(Exception ee){
			log.error(ee.getMessage());
			ee.printStackTrace();
		}
		return sms;
	}
	
	/**
	 * 修改短信信息
	 * @param sms
	 * @return
	 */
	public Sms updateSms(Sms sms){
		Sms msg = null;
		try{
			msg = (Sms)rdbSvc.save(sms);//save方法同时也是一个更新方法
			log.info("更新短信成功!id:"+sms.getId());
		}catch(Exception ee){
			log.error(ee.getMessage());
			ee.printStackTrace();
		}
		return msg;
	}
}
