package party.dao;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;

import party.dao.domain.LoginUser;

public interface LoginUserDao extends BaseDao<LoginUser> {
	
	/**
	 * 根据类名查询所有的LoginUser用户
	 * @return
	 */
	@Query("MATCH (L:LoginUser {}) return L")
	public List<LoginUser> queryAllLoginUser();
	
	/**
	 * 根据节点类名跟节点属性名称查询LoginUser详细信息
	 * @param PropertiesName
	 * @return
	 */
	@Query("MATCH (L:LoginUser {loginName:{0},password:{1}})-[s]->(n) return s,n")
	public List getDetailOfLoginUser(String loginName,String password);
	
	
	/**
	 * 根据节点查询用户信息
	 * @param LoginUserId
	 * @return
	 */
	@Query("START w=node({0}) RETURN w")
	public Object getLoginUser(Long LoginUserId);
	
	/**
	 * 创建LoginUser--数据
	 * @param id
	 */
	@Query("START n=node({0}) MATCH (T:Table {name:'LoginUser'}) CREATE (T)-[lud:loginUserdata]->(n)")
	public void createRelationShipWithLoginUser(Long id);
	
	/**
	 * 创建跟Person的关系
	 * @param id
	 */
	@Query("START n=node({0}) MATCH(o:Table {table:'par_person'}) create (o)-[pd:persondata]->(n)")
	public void createRelationShipWithPerson(Long id);

	/**
	 * 根据Id删除LongUser == data 
	 * @param id
	 */
	@Query("START n=node({0}) MATCH ()-[lud]->(n) delete n,lud")
	public void deleteLoginUser(Integer id);
	
	/**
	 * 角色创建
	 * @param startId
	 * @param endId
	 */
	@Query("START startNode = node({0}),endNode = node({1}) CREATE (endNode)-[b:belong]->(startNode)")
	public void createRelationShipLoginUserAndRole(Long startId,Long endId);
	
	
	/**
	 * LoginUser跟Party建立关系
	 * @param startId
	 * @param endId
	 */
	@Query("START startNode=node({0}),endNode = node({1}) CREATE (endNode)-[o:orgs]->(startNode)")
	public void createRelationShipLoginUserAndOrg(Long startId,Long endId);
	
	/**
	 * 删除LoginUser跟Org的关系
	 * @param id
	 */
	@Query("START n=node({0}) MATCH ()-[o]->(n) delete o")
	public void deleteLoginUserAndOrgs(Long id);
	
	
	/**
	 * 创建LoginUser跟Person关系
	 * @param startId
	 * @param endId
	 */
	@Query("START startNode=node({0}),endNode = node({1}) CREATE (endNode)-[b:belong]->(startNode)")
	public void createRelationShipPersonWithLoginUser(Long startId,Long endId);
	
	/**
	 * 修改密码
	 * @param LoginName
	 * @param password
	 * @param newpassword
	 * @return 
	 */
	@Query("START n=node(*) where n.loginName={0} and n.password={1} set n.password={2} return n")
	public Object updatepassword(String LoginName,String password,String newpassword);
	
	
	
	
}
