package promotion.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import promotion.dao.StoreTicketDao;
import promotion.dao.domain.StoreTicket;

@RestController
@RequestMapping(value="/sale/storeTicket")
public class StoreTicketAction {
	@Autowired
	private StoreTicketDao storeTicketDao;
	
	Logger log = Logger.getLogger(StoreTicketAction.class);
	
	/**
	 * 查询单条商城券信息
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/{id}",method={RequestMethod.GET})
	public StoreTicket findStoreTicketById(@PathVariable Integer id){
		log.info("查询id为："+id+"的范围");
		return storeTicketDao.getFromId(id);
	}
	
	/**
	 * 查询商城券列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.GET})
	public List<StoreTicket> findAll(){
		List<StoreTicket> list = new ArrayList<StoreTicket>();
		list = storeTicketDao.findAllStoreTicket();
		return list;
	}
	
	/**
	 * 保存商城券信息
	 * @param p
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.POST})
	public StoreTicket save(@RequestBody StoreTicket p){
		
		storeTicketDao.save(p);//创建节点
		storeTicketDao.createRelationship(p.getId().intValue(), 34, "DATA");//建立关系，24为模板节点的id,DATA为关系名
		
		return p;
	}
	
	/**
	 * 删除商城券数据以及关系
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/{id}",method={RequestMethod.DELETE})
	public String deleteStoreTicket(@PathVariable Integer id){
		log.info("删除节点id为："+id+"的数据");
		Object o = storeTicketDao.deleteRelationships(id);//删除该数据，并且删除关系
		return "123";
	}
	
	/**
	 * 修改商城券信息
	 * @param p
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.PUT})
	public StoreTicket upStoreTicket(StoreTicket p){
		storeTicketDao.save(p);
		return p;
	}
}
