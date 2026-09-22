package com.own.workflow.service;

import java.util.List;
import java.util.Map;


import com.own.workflow.dao.RdbBaseDao;
import com.own.workflow.domain.IDomainBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

@Slf4j
@Service
public class RdbSvc{
 	 
	@Autowired
	private RdbBaseDao baseDao;//这里借用IPartyOrgDao来调用通用方法，其实任何一个dao都可以
	
	public RdbBaseDao getBaseDao() {
		return baseDao;
	}
	public void setBaseDao(RdbBaseDao baseDao) {
		this.baseDao = baseDao;
	}
	/**
	 * 通用的保存实体方法
	 * 
	 */
	@Transactional
	public Object save(Object obj){
		IDomainBase objPo = requireDomain(obj);
 
		if(objPo.getObjectId()!=null){
			objPo = (IDomainBase)baseDao.find(obj,objPo.getObjectId());
			if (objPo == null) {
				throw new EntityNotFoundException("Entity to update does not exist");
			}
			BeanUtils.copyProperties(obj,objPo);
			baseDao.save(objPo);
			return objPo;
		}else{
 			baseDao.save(obj);
 			return obj;
		}
 	}
	/**
	 * 通用的保存实体方法
	 * @param obj
	 * @return
	 */
	@Transactional
	public Object update(Object obj){
		IDomainBase domain = requireDomain(obj);
		if (domain.getObjectId() == null) {
			throw new IllegalArgumentException("Entity identifier is required for update");
		}
		Object objPo = baseDao.find(obj, domain.getObjectId());
		if (objPo == null) {
			throw new EntityNotFoundException("Entity to update does not exist");
		}
		BeanUtils.copyProperties(obj,objPo);
		
		baseDao.save(objPo);
 		return objPo;
 	}
	
	/**
	 * 通用删除方法
	 * @param <T>
	 * @param obj
	 */
	public <T> void delete(Object obj){
		IDomainBase domain = requireDomain(obj);
		Object key = domain.getObjectId();
		if (key == null) {
			throw new IllegalArgumentException("Entity identifier is required for delete");
		}
		Object object = baseDao.find(obj, key);
		if (object == null) {
			throw new EntityNotFoundException("Entity to delete does not exist");
		}
		baseDao.remove(object);
	}
	
	/**
	 * 通用的查询指定实体方法
	 * @param obj 实体类型
	 * @param primaryKey 实体主键
	 * @return
	 */
	public Object find(Object obj){
		Object key = requireDomain(obj).getObjectId();
		Object returnO = baseDao.find(obj, key);
		return returnO;
	}
	
	//根据实体和key进行查找
	public Object find(Object obj,Object key){
		requireDomain(obj);
  		Object returnO = baseDao.find(obj, key);
		return returnO;
	}
	
	
	
	
	/**
	 * 通用查询列表的方法
	 * @param jsql
	 * @param countJsql
	 * @param paramsMap
	 * @param pageRequest
	 * @return
	 */
	
	/*public Object search(HSqlBean searchBean){
		
		String jsql=searchBean.getJsql();
		String countJsql=searchBean.getCountJsql();
		Map paramMap = searchBean.getParamsMap();
		
		Object result =null;
		if(countJsql==null||countJsql.trim().length()==0){
			result = findAll2Page(jsql,paramMap);
		}else{
			PageRequest pageRequest = searchBean.getPageRequest();
			result = findAllByConditions(jsql,countJsql,paramMap,pageRequest);
		}
  		return result;
 	}*/
	
	/*
	 * 返回所有的查询数据不分页
	 * 
	 * 
	 * 
	 */
	public Object findAll2Page(String jsql, Map paramMap) {
		Object result = baseDao.findAll2Page(jsql,paramMap);
		return result;
	}
	public Object findAllByConditions(String jsql, String countJsql,Map<String, ?> paramsMap, PageRequest pageRequest) {
		return baseDao.findPage(jsql, countJsql, paramsMap, pageRequest);
	}
 	
	@Transactional
	public int exeSql(String sql,Map params){
		int count = baseDao.exeNativeUpdate(sql, params);
		return count;
	}
	
	public Object findObject(String jsql,Map paramMap){
		return baseDao.findObject(jsql, paramMap);
	}
	
	public List<?> findList(String jsql,Map paramMap){
		return baseDao.findList(jsql, paramMap);
	}

	private IDomainBase requireDomain(Object obj) {
		if (!(obj instanceof IDomainBase)) {
			throw new IllegalArgumentException("Entity must implement IDomainBase");
		}
		return (IDomainBase) obj;
	}
}
