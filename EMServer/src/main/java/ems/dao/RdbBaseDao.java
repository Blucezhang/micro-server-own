package ems.dao;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;



@Repository
public class RdbBaseDao{

	
	@Autowired 
	protected EntityManager em;  

	
	/* (non-Javadoc)
	 * @see rdb.dao.base.IBaseDao#saveCommonEntry(java.lang.Object)
	 */
	public <T> T save(T obj){
		em.persist(obj);
		return obj;
	}
	/* (non-Javadoc)
	 * @see rdb.dao.base.IBaseDao#findCommonEntry(java.lang.Class, java.lang.Object)
	 */
	 
	public Object  find(Object obj,Object key){
		Class<? extends Object> clazz = obj.getClass();
	 		return em.find(clazz, key);
	}
	/* (non-Javadoc)
	 * @see rdb.dao.base.IBaseDao#removeCommonEntry(java.lang.Object)
	 */
	public void remove(Object obj){
		em.remove(obj);
	}
	
	/* (non-Javadoc)
	 * @see rdb.dao.base.IBaseDao#findAllByConditions(java.lang.String, java.util.Map, org.springframework.data.domain.Pageable)
	 */
	public Page<?> findPage(String jsql, String countJsql, Map<String, ?> paramsMap, Pageable pageable){
		Integer count = 0;
		if(countJsql!=null&&!"".equals(countJsql)){
			Query countQuery = em.createQuery(countJsql);  
			if(paramsMap!=null){
				for (Entry<String, ?> entry: paramsMap.entrySet()) {
					countQuery.setParameter(entry.getKey(), entry.getValue());
				}
			}
			count = Integer.valueOf(countQuery.getSingleResult().toString());
		}
		
		Query query = em.createQuery(jsql);  
	 
		if(paramsMap!=null){
			for (Entry<String, ?> entry: paramsMap.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
		}
		query.setFirstResult(pageable.getOffset());
		query.setMaxResults(pageable.getPageSize());
		List<?> list = query.getResultList();
		
		Page<?> page = new PageImpl(list,pageable,count);
		return page;
	}
	
	
	/* (non-Javadoc)
	 * @see rdb.dao.base.IBaseDao#findAllByConditions(java.lang.String, java.util.Map, org.springframework.data.domain.Pageable)
	 */
	public Page<?> findAll2Page(String jsql,Map<String, ?> paramsMap){
		 
		Query query = em.createQuery(jsql);  
	 
		if(paramsMap!=null){
			for (Entry<String, ?> entry: paramsMap.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
		}
 		List<?> list = query.getResultList();
   		Page<?> page = new PageImpl(list); 
		return page;
	}
	
	
	public List<?> findList(String jsql,Map<String, ?> paramsMap){
		 
		Query query = em.createQuery(jsql);  
	 
		if(paramsMap!=null){
			for (Entry<String, ?> entry: paramsMap.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
		}
 		List<?> list = query.getResultList();
   		 
		return list;
	}
	
	public Object findObject(String jsql,Map<String, ?> paramsMap){
		 
		Query query = em.createQuery(jsql);  
	 
		if(paramsMap!=null){
			for (Entry<String, ?> entry: paramsMap.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
		}
 		 
		return query.getSingleResult();
 	}
	
	
	
	
	

	/* (non-Javadoc)
	 * @see rdb.dao.base.IBaseDao#findAllByConditions(java.lang.String, java.lang.String, org.springframework.data.domain.Pageable)
	 */
	public Page<?> findAllByConditions(String jsql, String countJsql,Pageable pageable) {
		
		return findPage(jsql, countJsql, null, pageable);
	}

	/* (non-Javadoc)
	 * @see rdb.dao.base.IBaseDao#exeNativeUpdate(java.lang.String, java.util.Map)
	 */
	public int exeNativeUpdate(String sql, Map<String, ?> paramsMap) {
		Query query = em.createNativeQuery(sql);  
		if(paramsMap!=null){
			for (Entry<String, ?> entry: paramsMap.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
		}
		
		int rec = 0;
		
		try{
			rec = query.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
 		}finally{
 		    return rec;
		}
 	}
	
	/**
	 * @param sql
	 * @param paramsMap
	 * @param pageable
	 * @return
	 */
	public Page<?> findAllByNativeSql(String sql,String countSql, Map<String, ?> paramsMap, Pageable pageable){
		Integer count = 0;
		if(countSql!=null&&!"".equals(countSql)){
			Query countQuery = em.createNativeQuery(countSql);  
			if(paramsMap!=null){
				for (Entry<String, ?> entry: paramsMap.entrySet()) {
					countQuery.setParameter(entry.getKey(), entry.getValue());
				}
			}
			count = Integer.valueOf(countQuery.getSingleResult().toString());
		}
		
		Query query = em.createNativeQuery(sql);  
//		Query query = em.createNativeQuery(sql,"java.util.HashMap").unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);  

		if(paramsMap!=null){
			for (Entry<String, ?> entry: paramsMap.entrySet()) {
				query.setParameter(entry.getKey(), entry.getValue());
			}
		}
		query.setFirstResult(pageable.getOffset());
		query.setMaxResults(pageable.getPageSize());
		List<Object[]> list = query.getResultList();
		
		if(list==null||list.size()==0)
			return null;
		List<Map<String,Object>> resList = genarateResultList(sql,list);
		Page<?> page = new PageImpl(resList,pageable,count);
		return page;
	}
	/**
	 * 处理结果集（由于原生sql返回的是数组，这里将结果集转换成map）
	 */
	private List<Map<String,Object>> genarateResultList(String sql,List<Object[]> list){
		String sqlUpper = sql.toUpperCase();
		String[] keys = sqlUpper.substring(sqlUpper.indexOf("SELECT")+1,sqlUpper.indexOf("WHERE")).split(",");
		for (int i=0;i<keys.length;i++) {
			keys[i] = keys[i].indexOf(" AS ")>-1?keys[i].substring(keys[i].indexOf(" AS ")+3).trim():keys[i].trim();//如果没有as，则是原字符串
		}
		List<Map<String,Object>> resList = new ArrayList<Map<String,Object>>();
		for (Object[] obj : list) {
			Map<String,Object> rowMap = new HashMap<String,Object>();
			for(int i=0;i<obj.length;i++){
				rowMap.put(keys[i], obj[i]);
			}
			resList.add(rowMap);
		}
		return resList;
	}

  	 
	public int exeSql(String sql, Map<String,?> paramsMap) {
		//执行hsql
	 		 
			Query query = em.createNamedQuery(sql);  
			if(paramsMap!=null){
				for (Entry<String, ?> entry: paramsMap.entrySet()) {
					query.setParameter(entry.getKey(), entry.getValue());
				}
			}
			return query.executeUpdate();
	 }
	
 }
