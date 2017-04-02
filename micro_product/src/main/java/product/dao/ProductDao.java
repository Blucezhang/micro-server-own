package product.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.annotation.Query;

import product.dao.domain.Product;
import product.dao.domain.Template;

public interface ProductDao extends BaseDao<Product>{

	/* Product Start */
	@Query("MATCH (n:Product {}) return n")
    public List<Product> queryProduct();
	
	@Query("start n=node({0}) return n")
    public Product queryProductById(Long id);
	
	@Query("start n=node({0}) return n")
    public List<Product> queryProductByIds(String ids);
	
	@Query("start startNode=node({0}),endNode=node({1}) create(endNode)-[r:product]->(startNode)")
	public void createRelation(Long startId,Long endId);
	
	@Query("start p=node({0}) match()-[r]->(p) delete r,p")
	public void deleteProduct(Long startId);
	
	@Query("create(n:Product {0}) return n")
	public Product createProduct(Map map);
	/* Product End */
	
	/* Template Start */
	@Query("create(n:Template {0}) return n")
	public Template createTemplate(Map map);
	
	@Query("start n=node({0}) return n")
    public Template queryTemplateById(Long id);
	
	@Query("MATCH (n:Template {}) return n")
    public List<Template> queryTemplate();
	
	@Query("start startNode=node({0}),endNode=node({1}) create(endNode)-[r:template]->(startNode)")
	public void createTemplateRelation(Long startId,Long endId);
	/* Template End */
}
