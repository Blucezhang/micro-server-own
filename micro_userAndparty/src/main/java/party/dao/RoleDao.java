package party.dao;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;

import party.dao.domain.Role;

public interface RoleDao extends BaseDao<Role> {
		
	
	
	/**
	 * 查询所有的Role
	 * @return
	 */
	@Query("MATCH (R:Role {}) RETURN R")
	public List<Role> getAllRole();
	
	/**
	 * 创建Role数据
	 * @param id
	 */
	@Query("START n = node({0}) MATCH (T:Table {table:'sys_role'}) CREATE (T)-[rd:roledata]->(n)")
	public void createRelationShipWithRole(Long id);
	
	/**
	 * 删除Person === Data
	 * @param id
	 */
	@Query("START n=node({0}) MATCH ()-[rd]->(n) DELETE n,rd")
	public void deleteFun(Long id);
	
	/**
	 * 角色分配功能
	 * @param endNode funids
	 * @param startNode roleid
	 */
	@Query("START startNode = node({0}),endNode = node({1}) CREATE (endNode)-[flr:FbelongR]->(startNode)")
	public void createRelationShipRoleAndFun(Long endNode ,Long startNode);
	
	
	/**
	 * 删除功能与权限的关系
	 * @param id
	 */
	@Query("START n=node({0}) MATCH ()-[flr]->(n) DELETE flr")
	public void deleteRoleAndFunRelationShip(Long id);
	
	
	/**
	 * 部门角色
	 * @param endNode
	 * @param startNode
	 */
	@Query("START startNode = node({0}),endNode = node({1}) CREATE (endNode)-[rbo:RbelongO]->(startNode)")
	public void createRelationShipRoleAndOrg(Long endNode,Long startNode);
	
	
	/**
	 * 删除部门跟角色的关系
	 * @param Id
	 */
	@Query("START n=node({0}) MATCH ()-[rbo]->(n) DELETE rbo")
	public void deleteRoleAndOrgRelationShip(Long Id);

	
}
