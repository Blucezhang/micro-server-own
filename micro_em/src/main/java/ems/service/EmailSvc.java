package ems.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ems.domain.Email;

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
	 * @param wheresql
	 * @return
	 */
	public List<?> getEmails(String wheresql) {
		Map<String,Object> param = new HashMap<String,Object>();
		String findSmsSql = " from Email e where 1=1 ";
		Page<?> result = (Page<?>)rdbSvc.findAll2Page(findSmsSql+wheresql, param);
		return  result.getContent();
	}
}
