package product.dao;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface BaseDao<T> extends GraphRepository<T> {
	
	/**
	 * 数据库提前插入信息
	 * 1. create (n:DataBase {name:'DataBase'}) return n  //创建数据库
	 * 2. create (n:Table {name:'Person',table:'par_person'}) return n  创建person表
	 * 3. create (n:Table {name:'Org',table:'par_org'}) return n 创建org表
	 * 4. match(t:Table {table:'par_person'}),(d:DataBase {name:'DataBase'}) create (d)-[r:table]->(t) 创建table关系
	 * 5. match(t:Table {table:'par_org'}),(d:DataBase {name:'DataBase'}) create (d)-[r:table]->(t) 创建table关系
	 */
	
	/**
	 * 根据id查询信息
	 * {0} 表示 传入的参数 id
	 * @param id
	 * @return T
	 */
	@Query("MATCH (n) WHERE id(n)={0} RETURN n")
    public <T> T getFromId(Integer id);
	
	/**
     * 创建两个节点之间的关系,节点关系由startNodeId指向endNodeId
     * @param startNodeId 节点id
     * @param endNodeId 节点id
     * @param relationShipType 关系类型
     * @return
     */
    @Query("START startNode=node({0}),endNode=node({1}) CREATE (startNode)-[:CONTAIN]->(endNode)")
	//@Query("MATCH (p:Person { name: {0} }), (o:Organization { name: {1} }) CREATE (p)-[:{2}]->(o)")
    public void createRelationship(Integer startNodeId,Integer endNodeId,String relationShipType);
    
    /**
     * 根据id删除节点（没有关系关联的节点）
     * @return
     */
    @Query("start n=node({0}) delete n")
    public <T> T deleteRelationship(Integer id);
    
    /**
     * 删除节点和节点关系
     * @param id 节点id
     * @param relationShip 关系类型
     * @return
     */
    @Query("START n=node({0}) "
    	 + "MATCH (n)-[r:{1}]-() delete n,r ")
    public <T> T deleteRelationship(Integer id,String relationShip);
    
    /**
     * 删除两个节点和节点关系
     * @param id 节点id
     * @param relationShip 关系类型
     * @return
     */
    @Query("START startNode=node({0}),endNode=node({1}) "
    	 + "MATCH (startNode)-[r:{2}]-(endNode) delete startNode,r,endNode ")
    public <T> T deleteRelationship(Integer startNodeId,Integer endNodeId,String relationShip);

}
