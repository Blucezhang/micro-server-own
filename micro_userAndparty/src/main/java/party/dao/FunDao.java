package party.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.annotation.Query;

import party.dao.domain.Fun;

public interface FunDao extends BaseDao<Fun> {
	
	/**
	 * 创建数据
	 * @param maps
	 * @return
	 */
	@Query("create (n {0}) return n")
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
	@Query("START n=node({0}) MATCH()-[fd]->(n) delete n,fd")
	public void deleteFun(Long id);
	
	/**
	 * 创建Fun--数据
	 * @param id
	 */
	@Query("START n=node({0}) MATCH(T:Table {name:'Fun'}) CREATE (T)-[fd:fundata]->(n)")
	public void createRelationShipOfFun(Long id);
	
	/**
	 * 根据ID查询用户的详细信息
	 * @param id
	 * @return 
	 */
	@Query("START n=node({0}) return n")
	public Fun queryOneFunbyId(Long id);
	
}
