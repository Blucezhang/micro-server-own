package com.own.promotion.dao;

import java.util.List;
import java.util.Map;

import com.own.promotion.dao.domain.Product;
import com.own.promotion.dao.domain.Scope;
import org.springframework.data.neo4j.annotation.Query;
public interface ProductDao extends BaseDao<Product>{

	/**
	 * 此处的商品是指参加活动的商品，下同
	 */
	/**
	 * 创建商品数据
	 * {0} 代表传入的参数
	 */
	@Query(" create (n:Product{0}) return n; ")
	public Map createProduct(Map map);
	
	/**
	 * 查询所有商品信息
	 * @return
	 */
	@Query(" match (n:Product) return n ;")
	public List<Product> findAllProduct();
	
	
	/**
	 * 查询商品
	 * @param id
	 * @return
	 */
	@Query(" match (n:Product {id:{0}}) return n; ")
	public Scope findProduct(Long id);

	
	/**
	 * 修改商品数据
	 * 使用 set n+={map}，this will add and update properties, while keeping existing ones.
	 * If use SET n = {map}  Set all properties. This will remove any existing properties.
	 * @param id,map
	 * @return
	 */
	@Query(" match (n:Product {id:{0}}) set n+={1}  return n; ")
	public Scope updateProduct(Long id, Map map);
	
	/**
	 * 为活动添加商品
	 * relationship:JOIN
	 */
	@Query("START startNode=node({0}),endNode=node({1}) CREATE (startNode)-[:JOIN]->(endNode)")
	public void createRelationshipJoin(Integer startNodeId, Integer endNodeId, String relationShipType);
	

}
