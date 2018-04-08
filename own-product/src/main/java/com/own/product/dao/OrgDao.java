package com.own.product.dao;

import java.util.List;
import java.util.Map;

import com.own.product.domain.Organization;
import org.springframework.data.neo4j.annotation.Query;


public interface OrgDao extends BaseDao<Organization> {
	
	
	/**
	 * 创建数据
	 * @param Map
	 */
	@Query("create (n {0}) return n")
    public Map createOrg(Map o);
	
	/**
	 * 查询所有组织机构
	 * @return
	 */
	@Query("MATCH (n:Organization {}) return n")
    public List<Organization> queryAllOrg();
	
	/**
	 * 查询组织机构最高级
	 * @param attributeName
	 * @return
	 */
	@Query("MATCH (n:Table {table:'par_org'})-[r:data]->(o) return o")
    public List<Organization> queryOrg();
	
	/**
	 * 查询组织机构子级
	 * @param id
	 * @return 
	 */
	@Query("start n=node({0}) match(n)-[r:LEVEL]->(c) return c")
    public List<Organization> queryChildOrg(Integer id);
	
	/**
	 * 根据节点实体类名和节点name属性查询信息
	 * @param attributeName 节点属性key
	 * @return T
	 */
	@Query("MATCH (n:Organization { name:{0} }) return n")
    public Organization getFromName(String attributeName);
	
	/**
	 * 创建组织机构节点关系
	 * @param id
	 */
	/*@Query("START n=node({0}) MATCH(o:Table {table:'par_org'}) create (o)-[r:{1}]->(n)")
	public void createRelationShipData(Long id,RelTypes knows);*/
	@Query("START n=node({0}) MATCH(o:Table {table:'par_org'}) create (o)-[r:data]->(n)")
	public void createRelationShipData(Long id);
 
	/**
	 * 创建组织机构子节点关系
	 * @param startNodeId
	 * @param endNodeId
	 */
	@Query("START startNode=node({0}),endNode=node({1}) create (startNode)-[r:LEVEL]->(endNode)")
	public void createChildRelationShip(Integer startNodeId, Long endNodeId);
	
	/**
	 * 删除org
	 * @param id
	 */
	@Query("START n=node({0}) MATCH()-[r]->(n) delete n,r")
	public void deleteOrg(Integer id);
}

