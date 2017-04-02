package promotion.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.annotation.Query;

import promotion.dao.domain.Product;
import promotion.dao.domain.Scope;
import promotion.dao.domain.Seller;

public interface SellerDao extends BaseDao<Seller>{
	
	/**
	 * 创建参与活动的卖家
	 * {0} 代表传入的参数
	 */
	@Query(" create (n:Seller{0}) return n; ")
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
	@Query(" match (n:Seller {id:{0}}) return n; ")
	public Seller findSeller(Long id);

	
	/**
	 * 修改参与活动的卖家
	 * 使用 set n+={map}，this will add and update properties, while keeping existing ones.
	 * If use SET n = {map}  Set all properties. This will remove any existing properties.
	 * @param id,map
	 * @return
	 */
	@Query(" match (n:Seller {id:{0}}) set n+={1}  return n; ")
	public Seller updateSeller(Long id,Map map);
	
	
	/**
	 * 关联参与活动的卖家
	 * relationship:JOIN
	 */
	@Query("START startNode=node({0}),endNode=node({1}) CREATE (startNode)-[:JOIN]->(endNode)")
	public void createRelationshipJoin(Integer startNodeId,Integer endNodeId,String relationShipType);
}
