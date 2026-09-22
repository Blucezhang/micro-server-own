package com.own.send.server.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.send.server.domain.CommonInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;


@Service
public class CommonInfoSvc {
	
	@Autowired
	private RdbSvc rdbSvc;
	
	Logger logger = LoggerFactory.getLogger(CommonInfo.class);
	
	/**
	 * 添加或修改公共信息表
	 * @param cif
	 * @return
	 */
	public CommonInfo save(CommonInfo cif){
		
		if(cif!=null){
			try{
				rdbSvc.save(cif);
			}catch(Exception e){
				logger.error("保存公共信息表失败", e);
			}
		}
		return cif;
	}
	
	/**
	 * 查询公共信息
	 * @return
	 */
	public List<?> findList(Integer id, String title, String content,
			String sendAccount, String receiveAccount){
		Map<String,Object> param = new HashMap<String,Object>();
		StringBuilder query = new StringBuilder(" from CommonInfo c where 1=1 ");
		if (id != null) {
			query.append(" and c.infoId=:id");
			param.put("id", id);
		}
		addLikeFilter(query, param, "c.title", "title", title);
		addLikeFilter(query, param, "c.content", "content", content);
		addLikeFilter(query, param, "c.sendAccount", "sendAccount", sendAccount);
		addLikeFilter(query, param, "c.receiveAccount", "receiveAccount", receiveAccount);
		Page<?> result = (Page<?>) rdbSvc.findAll2Page(query.toString(), param);
		return result.getContent();
	}

	private void addLikeFilter(StringBuilder query, Map<String, Object> param,
			String field, String name, String value) {
		if (value != null && !value.trim().isEmpty()) {
			query.append(" and ").append(field).append(" like :").append(name);
			param.put(name, "%" + value.trim() + "%");
		}
	}
}
