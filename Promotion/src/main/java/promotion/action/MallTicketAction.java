package promotion.action;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import promotion.dao.MallTicketDao;
import promotion.dao.domain.MallTicket;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value="/sale/mallTicket")
public class MallTicketAction {
	
	@Autowired
	private MallTicketDao mallTicketDao;
	
	Logger log = Logger.getLogger(MallTicketAction.class);
	
	/**
	 * 查询单条商城券信息
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/{id}",method={RequestMethod.GET})
	public MallTicket findMallTicketById(@PathVariable Integer id){
		log.info("查询id为："+id+"的范围");
		return mallTicketDao.getFromId(id);
	}
	
	/**
	 * 查询商城券列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.GET})
	public List<MallTicket> findAll(){
		List<MallTicket> list = new ArrayList<MallTicket>();
		list = mallTicketDao.findAllMallTicket();
		return list;
	}
	
	/**
	 * 保存商城券信息
	 * @param p
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.POST})
	public MallTicket save(@RequestBody MallTicket p){
		
		mallTicketDao.save(p);//创建节点
		mallTicketDao.createRelationship(p.getId().intValue(), 25, "DATA");//建立关系，24为模板节点的id,DATA为关系名
		
		return p;
	}
	
	/**
	 * 删除商城券数据以及关系
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/{id}",method={RequestMethod.DELETE})
	public String deleteMallTicket(@PathVariable Integer id){
		log.info("删除节点id为："+id+"的数据");
		Object o = mallTicketDao.deleteRelationships(id);//删除该数据，并且删除关系
		return "123";
	}
	
	/**
	 * 修改商城券信息
	 * @param p
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="",method={RequestMethod.PUT})
	public MallTicket upMallTicket(MallTicket p){
		mallTicketDao.save(p);
		return p;
	}
}
