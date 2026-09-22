package com.own.send.server.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.send.server.domain.Email;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


@ComponentScan(basePackages = "ems.dao.domain")
@Service
public class EmailSvc {
	
	@Autowired
	private RdbSvc rdbSvc;
	
	Logger log = Logger.getLogger(EmailSvc.class);
	
	/**
	 * 查询邮件
	 * @param id
	 * @return
	 */
	public Email findEmailById(Integer id){
		Email email = null;
		String findSmsHql = " from Email e where e.id=:id";
		Map map = new HashMap();
		map.put("id", id);
		try{
			email = (Email)rdbSvc.findObject(findSmsHql, map);
		}catch(Exception ee){
			log.error("查询email出现异常，原因："+ee.getMessage());
		}
		return email;
	}
	
	/**
	 * 保存或修改email信息
	 * @param email
	 * @return
	 */
	public Email save(Email email){
		try{
			email = (Email)rdbSvc.save(email);
		}catch(Exception ee){
			log.error("保存email出现异常，原因："+ee.getMessage());
		}
		return email;
	}
	
	/**
	 * 查询邮件列表
	 * @param id optional email id
	 * @param title optional title filter
	 * @param content optional content filter
	 * @return
	 */
	public List<?> getEmails(Integer id, String title, String content) {
		Map<String,Object> param = new HashMap<String,Object>();
		StringBuilder query = new StringBuilder(" from Email e where 1=1 ");
		if (id != null) {
			query.append(" and e.id=:id");
			param.put("id", id);
		}
		if (title != null && !title.trim().isEmpty()) {
			query.append(" and e.title like :title");
			param.put("title", "%" + title.trim() + "%");
		}
		if (content != null && !content.trim().isEmpty()) {
			query.append(" and e.content like :content");
			param.put("content", "%" + content.trim() + "%");
		}
		Page<?> result = (Page<?>)rdbSvc.findAll2Page(query.toString(), param);
		return  result.getContent();
	}
}
