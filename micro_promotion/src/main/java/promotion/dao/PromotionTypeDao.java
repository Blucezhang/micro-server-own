package promotion.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.annotation.Query;

import promotion.dao.domain.Promotion;
import promotion.dao.domain.PromotionType;

public interface PromotionTypeDao extends BaseDao<PromotionType>{
	/**
	 * 创建数据
	 * {0} 代表传入的参数
	 */
	@Query(" create (n:PromotionType{0}) return n; ")
	public Map createPromotionType(Map map);
	
	
	
	/**
	 * 查询所有信息
	 * @return
	 */
	@Query(" match (n:PromotionType) return n ;")
	public List<PromotionType> findAllPromotionType();
	
	
	/**
	 * 查询活动类型
	 * @param id
	 * @return
	 */
	@Query(" match (n:PromotionType {id:{0}}) return n; ")
	public Promotion findPromotionType(Long id);

	
	/**
	 * 修改单条数据
	 * 使用 set n+={map}，this will add and update properties, while keeping existing ones.
	 * If use SET n = {map}  Set all properties. This will remove any existing properties.
	 * @param id,map
	 * @return
	 */
	@Query(" match (n:PromotionType {id:{0}}) set n+={1}  return n; ")
	public Promotion updatePromotionType(Long id,Map map);
}
