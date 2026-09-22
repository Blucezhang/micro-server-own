package com.own.user.party.dao;

import java.util.List;

import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.dao.domain.Role;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginUserDao extends BaseDao<LoginUser> {
	
	/**
	 * 根据类名查询所有的LoginUser用户
	 * @return
	 */
	@Query("MATCH (L:LoginUser {}) return L")
	public List<LoginUser> queryAllLoginUser();
	
	/**
	 * 根据登录名查询用户。密码必须在服务层通过 BCrypt 校验，禁止进入数据库查询。
	 *
	 * @param loginName 登录名
	 * @return 用户，不存在时返回 null
	 */
	@Query("MATCH (L:LoginUser) WHERE L.loginName = $0 RETURN L LIMIT 1")
	LoginUser findByLoginName(String loginName);

	/** Roles are stored in the legacy graph as (Role)-[:belong]->(LoginUser). */
	@Query("MATCH (u:LoginUser) WHERE id(u) = $0 MATCH (r:Role)-[:belong]->(u) RETURN DISTINCT r")
	List<Role> findRolesByLoginUserId(Long loginUserId);

	/** Functions granted through (Fun)-[:FbelongR]->(Role)-[:belong]->(LoginUser). */
	@Query("MATCH (u:LoginUser) WHERE id(u) = $0 MATCH (f:Fun)-[:FbelongR]->(r:Role)-[:belong]->(u) RETURN DISTINCT f.name")
	List<String> findPermissionNamesByLoginUserId(Long loginUserId);
	
	
	/**
	 * 根据节点查询用户信息
	 * @param LoginUserId
	 * @return
	 */
	@Query("MATCH (w:LoginUser) WHERE id(w) = $0 RETURN w")
	public Object getLoginUser(Long LoginUserId);
	
	/**
	 * 创建LoginUser--数据
	 * @param id
	 */
	@Query("MATCH (n:LoginUser), (T:Table {name: 'LoginUser'}) WHERE id(n) = $0 CREATE (T)-[:loginUserdata]->(n)")
	public void createRelationShipWithLoginUser(Long id);
	
	/**
	 * 创建跟Person的关系
	 * @param id
	 */
	@Query("MATCH (n:LoginUser), (o:Table {table: 'par_person'}) WHERE id(n) = $0 CREATE (o)-[:persondata]->(n)")
	public void createRelationShipWithPerson(Long id);

	/**
	 * 根据Id删除LongUser == data 
	 * @param id
	 */
	@Query("MATCH (n:LoginUser) WHERE id(n) = $0 OPTIONAL MATCH ()-[lud]->(n) DELETE lud, n")
	public void deleteLoginUser(Integer id);
	
	/**
	 * 角色创建
	 * @param startId
	 * @param endId
	 */
	@Query("MATCH (startNode:LoginUser), (endNode:Role) WHERE id(startNode) = $0 AND id(endNode) = $1 CREATE (endNode)-[:belong]->(startNode)")
	public void createRelationShipLoginUserAndRole(Long startId, Long endId);

	/**
	 * Grants a graph role exactly once.  This is deliberately a MERGE instead
	 * of the legacy CREATE method so retries cannot duplicate the relationship.
	 */
	@Query("MATCH (startNode:LoginUser), (endNode:Role) WHERE id(startNode) = $0 AND id(endNode) = $1 MERGE (endNode)-[:belong]->(startNode)")
	void grantRoleToLoginUserIfAbsent(Long loginUserId, Long roleId);
	
	
	/**
	 * LoginUser跟Party建立关系
	 * @param startId
	 * @param endId
	 */
	@Query("MATCH (startNode:LoginUser), (endNode:Organization) WHERE id(startNode) = $0 AND id(endNode) = $1 CREATE (endNode)-[:orgs]->(startNode)")
	public void createRelationShipLoginUserAndOrg(Long startId, Long endId);
	
	/**
	 * 删除LoginUser跟Org的关系
	 * @param id
	 */
	@Query("MATCH (n:LoginUser) WHERE id(n) = $0 MATCH ()-[o]->(n) DELETE o")
	public void deleteLoginUserAndOrgs(Long id);
	
	
	/**
	 * 创建LoginUser跟Person关系
	 * @param startId
	 * @param endId
	 */
	@Query("MATCH (startNode:Person), (endNode:LoginUser) WHERE id(startNode) = $0 AND id(endNode) = $1 CREATE (endNode)-[:belong]->(startNode)")
	public void createRelationShipPersonWithLoginUser(Long startId, Long endId);
	
}
