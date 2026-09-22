package com.own.user.party.dao;

import java.util.List;
import java.util.Map;

import com.own.user.party.dao.domain.Fun;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FunDao extends BaseDao<Fun> {
	
	/**
	 * 创建数据
	 * @param maps
	 * @return
	 */
	@Query("CREATE (n:Fun) SET n = $0 RETURN n")
	public Map createFun(Map maps);
	
	/**
	 * 查询所有的功能
	 * @return
	 */
	@Query("MATCH (n:Fun{}) return n")
	public List<Fun> queryAllFun();
	
	/**
	 *根据ID删除功能信息
	 * @param id
	 */
	@Query("MATCH (n:Fun) WHERE id(n) = $0 OPTIONAL MATCH ()-[fd]->(n) DELETE fd, n")
	public void deleteFun(Long id);
	
	/**
	 * 创建Fun--数据
	 * @param id
	 */
	@Query("MATCH (n:Fun), (T:Table {name: 'Fun'}) WHERE id(n) = $0 CREATE (T)-[:fundata]->(n)")
	public void createRelationShipOfFun(Long id);
	
	/**
	 * 根据ID查询用户的详细信息
	 * @param id
	 * @return 
	 */
	@Query("MATCH (n:Fun) WHERE id(n) = $0 RETURN n")
	public Fun queryOneFunbyId(Long id);
	
}
