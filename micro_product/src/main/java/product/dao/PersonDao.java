package product.dao;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;

import product.dao.domain.Person;

public interface PersonDao extends BaseDao<Person>  {
	
	/**
	 * 通过类名查询所有用户
	 * @param attributeName
	 * @return
	 */
	@Query("MATCH (n:Person {}) return n")
    public List<Person> queryPerson();
	
	/**
	 * 根据节点实体类名和节点name属性查询信息
	 * @param attributeName 节点属性key
	 * @return T
	 */
	@Query("MATCH (n:Person { name:{0} }) return n")
    public Person getFromName(String attributeName);
	
	/**
	 * 创建用户节点关系
	 * @param id
	 */
	@Query("START n=node({0}) MATCH(o:Table {table:'par_person'}) create (o)-[r:data]->(n)")
	public void createRelationShipData(Long id);
	
	/**
     * 创建两个节点之间的关系,节点关系由startNodeId指向endNodeId
     * @param startNodeId 节点id
     * @param endNodeId 节点id
     * @param relationShipType 关系类型
     * @return
     */
    @Query("START startNode=node({0}),endNode=node({1}) CREATE (endNode)-[:CONTAIN]->(startNode)")
    //@Query("MATCH (p:Person { name: {0} }), (o:Organization { name: {1} }) CREATE (p)-[:{2}]->(o)")
    public void createRelationshipContain(Long startNodeId,Long endNodeId);
	
    /**
	 * 删除person
	 * @param id
	 */
	@Query("START n=node({0}) MATCH()-[r]->(n) delete n,r")
	public void deletePerson(Integer id);
}