package com.own.promotion.dao;


import java.util.List;
import java.util.Map;

import com.own.promotion.dao.domain.Promotion;
import org.springframework.data.neo4j.repository.query.Query;

public interface PromotionDao extends BaseDao {

	/**
	 * 创建数据
	 * {0} 代表传入的参数
	 */
	@Query("CREATE (n:Promotion) SET n = $0 RETURN n")
	public Map createPromotion(Map map);
	
	
	
	/**
	 * 查询所有信息
	 * @return
	 */
	@Query(" match (n:Promotion) return n ;")
	public List<Promotion> findAllPromotion();
	
	
	/**
	 * 查询活动
	 * @param id
	 * @return
	 */
	@Query("MATCH (n:Promotion) WHERE id(n) = $0 RETURN n")
	public Promotion findPromotion(Long id);

	
	
	@Query("MATCH (n:Promotion) WHERE id(n) = $0 RETURN n")
	public Object findNode(Long id);
	
	/**
	 * 修改单条数据
	 * 使用 set n+={map}，this will add and update properties, while keeping existing ones.
	 * If use SET n = {map}  Set all properties. This will remove any existing properties.
	 * @param id,map
	 * @return
	 */
	@Query("MATCH (n:Promotion) WHERE id(n) = $0 SET n += $1 RETURN n")
	public Promotion updatePromotion(Long id, Map map);
	
	
	
	
	/**
     * 创建两个节点之间的关系,节点关系由startNodeId指向endNodeId；此处用于创建，活动属于哪种方式、哪种范围
     * @param startNodeId 节点id
     * @param endNodeId 节点id
     * @param relationShipType 关系类型
     * @return
     */
    @Query("MATCH (startNode:Promotion), (endNode) WHERE id(startNode) = $0 AND id(endNode) = $1 CREATE (startNode)-[:BELONG]->(endNode)")
    public void createRelationshipBelong(Integer startNodeId, Integer endNodeId, String relationShipType);
    
    
    /**
     * 根据传入的商品id，查询对应的活动
     * @param productId
     * @return
     */
	    @Query("MATCH (p:Product)-[]->(m:Promotion)-[]->(type:PromotionType) WHERE p.productId = $0 RETURN m, type")
	    public List findProByProductInfo(String productId);
    
    
    /**
     * 根据传入的卖家id，查询对应的活动
     * @param productId
     * @return
     */
    @Query("MATCH (s:Seller)-[]->(m:Promotion) WHERE s.sellerId = $0 RETURN m")
    public List<Promotion> findProBySellerInfo(Long sellerId);
    
    
    /**
     * 根据促销类型节点查询活动。
     * @param typeId Neo4j 节点 id
     * @return matching promotions
     */
    @Query("MATCH (m:Promotion)-[:BELONG]->(pt:PromotionType) WHERE id(pt) = $0 RETURN m")
    public List<Promotion> findProByTypeInfo(Long typeId);
    
    
    /**
     * 根据活动范围节点查询活动。
     * @param zoneId Neo4j Scope 节点 id
     * @return matching promotions
     */
    @Query("MATCH (m:Promotion)-[:BELONG]->(z:Scope) WHERE id(z) = $0 RETURN m")
    public List<Promotion> findProByZoneInfo(Long zoneId);
    
    
    
}
