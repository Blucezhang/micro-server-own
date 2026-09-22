package com.own.user.party.dao;

import java.util.List;

import com.own.face.party.RoleBean;
import com.own.user.party.dao.domain.Role;
import org.springframework.data.neo4j.repository.query.Query;


public interface RoleDao extends BaseDao<Role> {


	/**
	 * 查询所有的Role
	 * @return
	 */
	@Query("MATCH (R:Role {}) RETURN R")
	public List<Role> getAllRole();

	/**
	 * Marketplace roles were introduced after the legacy graph.  Accept the
	 * historical "merchant" spelling as well as the canonical claim name.
	 */
	@Query("MATCH (r:Role) WHERE toUpper(r.name) IN ['MERCHANT','ROLE_MERCHANT'] RETURN r ORDER BY id(r) ASC LIMIT 1")
	Role findMarketplaceMerchantRole();

	/**
	 * 创建Role数据
	 * @param id
	 */
	@Query("MATCH (n:Role), (T:Table {table: 'sys_role'}) WHERE id(n) = $0 CREATE (T)-[:roledata]->(n)")
	public void createRelationShipWithRole(Long id);

	/**
	 * 删除Person === Data
	 * @param id
	 */
	@Query("MATCH (n:Role) WHERE id(n) = $0 OPTIONAL MATCH ()-[rd]->(n) DELETE rd, n")
	public void deleteFun(Long id);

	/**
	 * 角色分配功能
	 * @param endNode funids
	 * @param startNode roleid
	 */
	@Query("MATCH (startNode:Fun), (endNode:Role) WHERE id(startNode) = $0 AND id(endNode) = $1 MERGE (endNode)-[:FbelongR]->(startNode)")
	public void createRelationShipRoleAndFun(Long endNode ,Long startNode);


	/**
	 * 删除功能与权限的关系
	 * @param id
	 */
	@Query("MATCH (n:Role) WHERE id(n) = $0 MATCH ()-[flr]->(n) DELETE flr")
	public void deleteRoleAndFunRelationShip(Long id);


	/**
	 * 部门角色
	 * @param endNode
	 * @param startNode
	 */
	@Query("MATCH (startNode:Organization), (endNode:Role) WHERE id(startNode) = $0 AND id(endNode) = $1 CREATE (endNode)-[:RbelongO]->(startNode)")
	public void createRelationShipRoleAndOrg(Long endNode,Long startNode);


	/**
	 * 删除部门跟角色的关系
	 * @param Id
	 */
	@Query("MATCH (n:Role) WHERE id(n) = $0 MATCH ()-[rbo]->(n) DELETE rbo")
	public void deleteRoleAndOrgRelationShip(Long Id);


}
