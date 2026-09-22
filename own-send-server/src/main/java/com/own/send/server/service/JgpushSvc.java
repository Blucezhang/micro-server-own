package com.own.send.server.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.send.server.domain.Jgpush;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JgpushSvc {

	Logger log = LoggerFactory.getLogger(JgpushSvc.class);
	
	@Autowired
	private RdbSvc rdbSvc;
	
	/**
	 * 保存或修改信息
	 * @param jp
	 * @return
	 */
	public Jgpush save(Jgpush jp){
		try{
			rdbSvc.save(jp);
		}catch(Exception ee){
			log.error("极光推送记录保存失败", ee);
		}
		return jp;
	}
	
	/**
	 * 根据id查询极光信息
	 * @param id
	 * @return
	 */
	public Jgpush findJgpushById(Integer id){
		String findsql = " from Jgpush jp where jp.id=:id ";
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("id", id);
		Jgpush jp = null;
		try{
			jp = (Jgpush)rdbSvc.findObject(findsql,param);
		}catch(Exception ee){
			log.error("极光推送记录查询失败", ee);
		}
		return jp;
	}
	
	/**
	 * 查询列表信息
	 * @param title optional title filter
	 * @param content optional content filter
	 * @return
	 */
	public List<?> findJgpushs(String title, String content){
		StringBuilder findsql = new StringBuilder(" from Jgpush jp where 1=1 ");
		Map<String,Object> param = new HashMap<String,Object>();
		if (title != null && !title.trim().isEmpty()) {
			findsql.append(" and jp.title like :title");
			param.put("title", "%" + title.trim() + "%");
		}
		if (content != null && !content.trim().isEmpty()) {
			findsql.append(" and jp.content like :content");
			param.put("content", "%" + content.trim() + "%");
		}
		List<?> jlist = new ArrayList<Object>();
		try{
			jlist = rdbSvc.findList(findsql.toString(), param);
		}catch(Exception ee){
			log.error("极光推送记录列表查询失败", ee);
		}
		return jlist;
	}
}
