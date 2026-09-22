package com.own.product.dao;

import java.util.List;
import java.util.Map;

import com.own.product.domain.Organization;
import org.springframework.data.neo4j.repository.query.Query;


public interface OrgDao extends BaseDao<Organization> {
	
	
	/**
	 * 创建数据
	 */
	@Query("CREATE (n:Organization) SET n = $0 RETURN n")
    public Map createOrg(Map o);
	
	/**
	 * 查询所有组织机构
	 * @return
	 */
	@Query("MATCH (n:Organization {}) return n")
    public List<Organization> queryAllOrg();
	
	/**
	 * 查询组织机构最高级
	 * @return
	 */
	@Query("MATCH (n:Table {table:'par_org'})-[r:data]->(o) return o")
    public List<Organization> queryOrg();
	
	/**
	 * 查询组织机构子级
	 * @param id
	 * @return 
	 */
	@Query("MATCH (n:Organization) WHERE id(n) = $0 MATCH (n)-[:LEVEL]->(c:Organization) RETURN c")
    public List<Organization> queryChildOrg(Integer id);
	
	/**
	 * 根据节点实体类名和节点name属性查询信息
	 * @param attributeName 节点属性key
	 * @return T
	 */
	@Query("MATCH (n:Organization) WHERE n.name = $0 RETURN n")
    public Organization getFromName(String attributeName);
	
	/**
	 * 创建组织机构节点关系
	 * @param id
	 */
	@Query("MATCH (n:Organization), (o:Table {table: 'par_org'}) WHERE id(n) = $0 CREATE (o)-[:data]->(n)")
	public void createRelationShipData(Long id);
 
	/**
	 * 创建组织机构子节点关系
	 * @param startNodeId
	 * @param endNodeId
	 */
	@Query("MATCH (startNode:Organization), (endNode:Organization) WHERE id(startNode) = $0 AND id(endNode) = $1 CREATE (startNode)-[:LEVEL]->(endNode)")
	public void createChildRelationShip(Integer startNodeId, Long endNodeId);
	
	/**
	 * 删除org
	 * @param id
	 */
	@Query("MATCH (n:Organization) WHERE id(n) = $0 OPTIONAL MATCH ()-[r]-(n) DELETE r, n")
	public void deleteOrg(Integer id);
}
