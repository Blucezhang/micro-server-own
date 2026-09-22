package com.own.product.dao;


import com.own.product.domain.Category;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;


public interface CategoryDao extends BaseDao<Category>{
	
	/**
	 * 根据级别查询类别
	 * @return List<Category>
	 */
	@Query("MATCH (n:Category) WHERE n.level = $0 RETURN n")
    public List<Category> queryCategoryByLevel(String level);
	
	/**
	 * 根据id查询类别
	 * @return Category
	 */
	@Query("MATCH (n:Category) WHERE id(n) = $0 RETURN n")
    public Category queryCategoryById(Long id);
	
	/**
	 * 创建类别关系
	 * @param startId
	 * @param endId
	 */
	@Query("MATCH (startNode:Category), (endNode:Category) WHERE id(startNode) = $0 AND id(endNode) = $1 CREATE (endNode)-[:category]->(startNode)")
	public void createRelation(Long startId, Long endId);
	
	@Query("MATCH (p:Category) WHERE id(p) = $0 OPTIONAL MATCH ()-[r]->(p) DELETE r, p")
	public void deleteCategory(Long startId);
	
}
