package com.own.promotion.dao;

import java.util.List;
import java.util.Map;

import com.own.promotion.dao.domain.Scope;
import com.own.promotion.dao.domain.StoreTicket;
import org.springframework.data.neo4j.annotation.Query;


public interface StoreTicketDao extends BaseDao<StoreTicket>{
	/**
	 * 创建店铺券数据
	 * {0} 代表传入的参数
	 */
	@Query(" create (n:StoreTicket{0}) return n; ")
	public Map createStoreTicket(Map map);
	
	
	
	/**
	 * 查询所有店铺券信息
	 * @return
	 */
	@Query(" match (n:StoreTicket) return n ;")
	public List<StoreTicket> findAllStoreTicket();
	
	
	/**
	 * 查询店铺券
	 * @param id
	 * @return
	 */
	@Query(" match (n:StoreTicket {id:{0}}) return n; ")
	public StoreTicket findStoreTicket(Long id);

	
	/**
	 * 修改店铺券
	 * 使用 set n+={map}，this will add and update properties, while keeping existing ones.
	 * If use SET n = {map}  Set all properties. This will remove any existing properties.
	 * @param id,map
	 * @return
	 */
	@Query(" match (n:StoreTicket {id:{0}}) set n+={1}  return n; ")
	public Scope updateStoreTicket(Long id, Map map);
}
