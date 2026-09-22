package com.own.product.dao;

import java.util.List;
import java.util.Map;

import com.own.product.domain.Product;
import com.own.product.domain.Template;
import org.springframework.data.neo4j.repository.query.Query;


public interface ProductDao extends BaseDao<Product>{

	/* Product Start */
	@Query("MATCH (n:Product {}) return n")
    public List<Product> queryProduct();
	
	@Query("MATCH (n:Product) WHERE id(n) = $0 RETURN n")
    public Product queryProductById(Long id);
	
	@Query("MATCH (n:Product) WHERE id(n) = $0 RETURN n")
    public List<Product> queryProductByIds(String ids);
	
	@Query("MATCH (startNode:Product), (endNode:Category) WHERE id(startNode) = $0 AND id(endNode) = $1 CREATE (endNode)-[:product]->(startNode)")
	public void createRelation(Long startId, Long endId);
	
	@Query("MATCH (p:Product) WHERE id(p) = $0 OPTIONAL MATCH ()-[r]->(p) DELETE r, p")
	public void deleteProduct(Long startId);
	
	@Query("CREATE (n:Product) SET n = $0 RETURN n")
	public Product createProduct(Map map);

	@Query("CREATE (n:Template) SET n = $0 RETURN n")
	public Template createTemplate(Map map);
	
	@Query("MATCH (n:Template) WHERE id(n) = $0 RETURN n")
    public Template queryTemplateById(Long id);
	
	@Query("MATCH (n:Template {}) return n")
    public List<Template> queryTemplate();
	
	@Query("MATCH (startNode:Template), (endNode:Category) WHERE id(startNode) = $0 AND id(endNode) = $1 CREATE (endNode)-[:template]->(startNode)")
	public void createTemplateRelation(Long startId, Long endId);
}
