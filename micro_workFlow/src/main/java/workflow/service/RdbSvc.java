package workflow.service;




import java.util.List;
import java.util.Map;




import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import workflow.dao.RdbBaseDao;
import workflow.domain.IDomainBase;
 

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
		IDomainBase objPo =  (IDomainBase)obj ;
 
		if(objPo.getObjectId()!=null){
			objPo = (IDomainBase)baseDao.find(obj,objPo.getObjectId());
			if(objPo==null)
				//throw new IFException("要更新的实体记录不存在！");
				System.out.println("要更新的实体记录不存在！");
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
		Object objPo = baseDao.find(obj, ((IDomainBase)obj).getObjectId());
		if(objPo==null)
			//throw new IFException("要更新的实体记录不存在！");
		System.out.println("要更新的实体记录不存在！");
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
		if(!(obj instanceof IDomainBase))
				//throw new IFException("该实体没有继承IDomainBase！！！");
		System.out.println("该实体没有继承IDomainBase！！！");
		Object key = ((IDomainBase)obj).getObjectId();
		Object object = baseDao.find(obj, key);
		if(obj==null)
			//throw new IFException("要删除的实体不存在或已被删除！");
			System.out.println("要删除的实体不存在或已被删除！");
		baseDao.remove(object);
	}
	
	/**
	 * 通用的查询指定实体方法
	 * @param clazz 实体类型
	 * @param primaryKey 实体主键
	 * @return
	 */
	public Object find(Object obj){
		if(!(obj instanceof IDomainBase))
			//throw new IFException("该实体没有继承IDomainBase！！！");
			System.out.println("该实体没有继承IDomainBase！！！");
		Object key = ((IDomainBase)obj).getObjectId();
		Object returnO = baseDao.find(obj, key);
		
//		if(returnO==null)
//			throw new IFException("找不到该实体的实例!!!");
		return returnO;
	}
	
	//根据实体和key进行查找
	public Object find(Object obj,Object key){
		if(!(obj instanceof IDomainBase))
				//throw new IFException("该实体没有继承IDomainBase！！！");
			System.out.println("该实体没有继承IDomainBase！！！");
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
}
