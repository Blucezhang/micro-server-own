package com.own.user.party.dao;

import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface BaseDao<T> extends CrudRepository<T, Long> {
	
	/**
	 * 数据库提前插入信息
	 * 1. create (n:DataBase {name:'DataBase'}) return n  //创建数据库
	 * 2. create (n:Table {name:'Person',table:'par_person'}) return n  创建person表
	 * 3. create (n:Table {name:'Org',table:'par_org'}) return n 创建org表
	 * 4. match  (t:Table {table:'par_person'}),(d:DataBase {name:'DataBase'}) create (d)-[r:table]->(t) 创建table关系
	 * 5. match  (t:Table {table:'par_org'}),(d:DataBase {name:'DataBase'}) create (d)-[r:table]->(t) 创建table关系
	 * 6. create (n:Table {name:'Fun',table:'sys_fun'}) return n 创建功能表 Fun
	 * 7.create (n:Table {name:'LoginUser',table:'sys_loginuser'}) return n 创建系统用户表LoginUser
	 * 8.create (n:Table {name:'Role',table:'sys_role'}) return n 创建角色表Role
	 * 9.match  (t:Table {table:'sys_fun'}),(d:DataBase {name:'DataBase'}) create (d)-[r:table]->(t)  创建Table关系
	 * 10.match  (t:Table {table:'sys_loginuser'}),(d:DataBase {name:'DataBase'}) create (d)-[r:table]->(t) 创建Table关系
	 * 11.match  (t:Table {table:'sys_role'}),(d:DataBase {name:'DataBase'}) create (d)-[r:table]->(t) 创建Table关系
	 * 
	 * 
	 * 1、start n=node(9) match(n)-[s]->() delete s 删除节点关系
	 * 
	 */
	
	/**
	 * 根据id查询信息
	 * 使用 Cypher 参数绑定传入节点 id。
	 * @param id
	 * @return T
	 */
	@Query("MATCH (n) WHERE id(n) = $0 RETURN n")
    public <T> T getFromId(Integer id);
	
	/**
     * 创建两个节点之间的关系,节点关系由startNodeId指向endNodeId
     * @param startNodeId 节点id
     * @param endNodeId 节点id
     * 固定创建 CONTAIN 关系；关系类型不得由请求参数决定。
     * @return
     */
    @Query("MATCH (startNode), (endNode) WHERE id(startNode) = $0 AND id(endNode) = $1 CREATE (startNode)-[:CONTAIN]->(endNode)")
    public void createContainRelationship(Integer startNodeId, Integer endNodeId);

    @Deprecated
    default void createRelationship(Integer startNodeId, Integer endNodeId, String relationShipType) {
        throw new UnsupportedOperationException("Dynamic relationship types are not supported; use createContainRelationship");
    }
    
    /**
     * 根据id删除节点（没有关系关联的节点）
     * @return
     */
    @Query("MATCH (n) WHERE id(n) = $0 DETACH DELETE n")
    public void deleteRelationship(Integer id);
    
    /**
     * 删除节点和节点关系
     * @param id 节点id
     * @param relationShip 关系类型
     * @return
     */
    @Deprecated
    default void deleteRelationship(Integer id, String relationShip) {
        throw new UnsupportedOperationException("Dynamic relationship types are not supported; use a repository method with a fixed relationship type");
    }
    
    /**
     * 删除两个节点和节点关系
     * @param id 节点id
     * @param relationShip 关系类型
     * @return
     */
    @Deprecated
    default void deleteRelationship(Integer startNodeId, Integer endNodeId, String relationShip) {
        throw new UnsupportedOperationException("Dynamic relationship types are not supported; use a repository method with a fixed relationship type");
    }

}
