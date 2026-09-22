package com.own.promotion.dao;

import java.util.List;
import java.util.Map;

import com.own.promotion.dao.domain.Seller;
import org.springframework.data.neo4j.repository.query.Query;


public interface SellerDao extends BaseDao<Seller>{
	
	/**
	 * 创建参与活动的卖家
	 * {0} 代表传入的参数
	 */
	@Query("CREATE (n:Seller) SET n = $0 RETURN n")
	public Map createSeller(Map map);
	
	/**
	 * 查询所有参加活动的卖家
	 * @return
	 */
	@Query(" match (n:Seller) return n ;")
	public List<Seller> findAllSeller();
	
	
	/**
	 * 查询参与活动的卖家
	 * @param id
	 * @return
	 */
	@Query("MATCH (n:Seller) WHERE id(n) = $0 RETURN n")
	public Seller findSeller(Long id);

	
	/**
	 * 修改参与活动的卖家
	 * 使用 set n+={map}，this will add and update properties, while keeping existing ones.
	 * If use SET n = {map}  Set all properties. This will remove any existing properties.
	 * @param id,map
	 * @return
	 */
	@Query("MATCH (n:Seller) WHERE id(n) = $0 SET n += $1 RETURN n")
	public Seller updateSeller(Long id, Map map);
	
	
	/**
	 * 关联参与活动的卖家
	 * relationship:JOIN
	 */
	@Query("MATCH (startNode:Seller), (endNode:Promotion) WHERE id(startNode) = $0 AND id(endNode) = $1 CREATE (startNode)-[:JOIN]->(endNode)")
	public void createRelationshipJoin(Integer startNodeId, Integer endNodeId, String relationShipType);
}
