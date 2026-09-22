package com.own.promotion.dao;

import java.util.List;
import java.util.Map;

import com.own.promotion.dao.domain.Scope;
import org.springframework.data.neo4j.repository.query.Query;

public interface ScopeDao extends BaseDao<Scope>{
	/**
	 * 创建范围数据
	 * {0} 代表传入的参数
	 */
	@Query("CREATE (n:Scope) SET n = $0 RETURN n")
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
	@Query("MATCH (n:Scope) WHERE id(n) = $0 RETURN n")
	public Scope findScope(Long id);

	
	/**
	 * 修改单条范围数据
	 * 使用 set n+={map}，this will add and update properties, while keeping existing ones.
	 * If use SET n = {map}  Set all properties. This will remove any existing properties.
	 * @param id,map
	 * @return
	 */
	@Query("MATCH (n:Scope) WHERE id(n) = $0 SET n += $1 RETURN n")
	public Scope updateScope(Long id, Map map);
	
}
