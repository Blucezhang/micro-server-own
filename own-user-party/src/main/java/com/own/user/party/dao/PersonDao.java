package com.own.user.party.dao;

import java.util.List;

import com.own.user.party.dao.domain.Person;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface PersonDao extends BaseDao<Person>  {
	
	/**
	 * 通过类名查询所有用户
	 * @return
	 */
	@Query("MATCH (n:Person {}) return n")
    public List<Person> queryPerson();
	
	/**
	 * 根据节点实体类名和节点name属性查询信息
	 * @return T
	 */
	@Query("MATCH (n:Person) WHERE n.name = $0 AND n.phone = $1 AND n.email = $2 RETURN n")
    public Person getFromName(String name, String phone, String email);
	
	
	/**
	 * 创建用户节点关系
	 * @param id
	 */
	@Query("MATCH (n:Person), (o:Table {table: 'par_person'}) WHERE id(n) = $0 CREATE (o)-[:data]->(n)")
	public void createRelationShipData(Long id);
	
	/**
     * 创建两个节点之间的关系,节点关系由startNodeId指向endNodeId
     * @param startNodeId 节点id
     * @param endNodeId 节点id
     * @param relationShipType 关系类型
     * @return
     */
    @Query("MATCH (startNode:Person), (endNode:Organization) WHERE id(startNode) = $0 AND id(endNode) = $1 CREATE (endNode)-[:CONTAIN]->(startNode)")
    public void createRelationshipContain(Long startNodeId, Long endNodeId);
	
    /**
	 * 删除person
	 * @param id
	 */
	@Query("MATCH (n:Person) WHERE id(n) = $0 OPTIONAL MATCH ()-[r]-(n) DELETE r, n")
	public void deletePerson(Integer id);
}
