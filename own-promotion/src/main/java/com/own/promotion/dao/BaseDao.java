package com.own.promotion.dao;

import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface BaseDao<T> extends CrudRepository<T, Long>{
	
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
     * 固定创建 DATA 关系；关系类型不得由请求参数决定。
     * @return
     */
    @Query("MATCH (startNode), (endNode) WHERE id(startNode) = $0 AND id(endNode) = $1 CREATE (startNode)-[:DATA]->(endNode)")
    public void createDataRelationship(Integer startNodeId, Integer endNodeId);

    @Deprecated
    default void createRelationship(Integer startNodeId, Integer endNodeId, String relationShipType) {
        throw new UnsupportedOperationException("Dynamic relationship types are not supported; use createDataRelationship");
    }
    
    /**
     * 根据id删除节点（没有关系关联的节点）
     * @return
     */
    @Query("MATCH (n) WHERE id(n) = $0 DETACH DELETE n")
    public void deleteRelationship(Integer id);
    
    /**
     * 删除节点和节点关系(指向别的节点的关系)
     * @param id 节点id
     * @return
     */
    @Query("MATCH (n) WHERE id(n) = $0 DETACH DELETE n")
    public void deleteRelationships(Integer id);
    
    
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
