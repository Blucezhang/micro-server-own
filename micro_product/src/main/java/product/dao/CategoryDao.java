package product.dao;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;

import product.dao.domain.Category;

public interface CategoryDao extends BaseDao<Category>{
	
	@Query("{0}")
    public List<Category> test(String cypher);

	/**
	 * 根据级别查询类别
	 * @return List<Category>
	 */
	@Query("MATCH (n:Category {level:{0}}) return n")
    public List<Category> queryCategoryByLevel(String level);
	
	/**
	 * 根据id查询类别
	 * @return Category
	 */
	@Query("start n=node({0}) return n")
    public Category queryCategoryById(Long id);
	
	/**
	 * 创建类别关系
	 * @param startId
	 * @param endId
	 */
	@Query("start startNode=node({0}),endNode=node({1}) create(endNode)-[r:category]->(startNode)")
	public void createRelation(Long startId,Long endId);
	
	@Query("start p=node({0}) match()-[r]->(p) delete r,p")
	public void deleteCategory(Long startId);
	
}
