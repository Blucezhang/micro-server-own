package com.own.promotion.dao;

import java.util.List;
import java.util.Map;

import com.own.promotion.dao.domain.Scope;
import org.springframework.data.neo4j.annotation.Query;

public interface ScopeDao extends BaseDao<Scope>{
	/**
	 * 创建范围数据
	 * {0} 代表传入的参数
	 */
	@Query(" create (n:Scope{0}) return n; ")
	public Map createScope(Map map);
	
	
	
	/**
	 * 查询所有范围信息
	 * @return
	 */
	@Query(" match (n:Scope) return n ;")
	public List<Scope> findAllScope();
	
	/**
	 * 查询范围
	 * @param id
	 * @return
	 */
	@Query(" match (n:Scope {id:{0}}) return n; ")
	public Scope findScope(Long id);

	
	/**
	 * 修改单条范围数据
	 * 使用 set n+={map}，this will add and update properties, while keeping existing ones.
	 * If use SET n = {map}  Set all properties. This will remove any existing properties.
	 * @param id,map
	 * @return
	 */
	@Query(" match (n:Scope {id:{0}}) set n+={1}  return n; ")
	public Scope updateScope(Long id, Map map);
	
}
