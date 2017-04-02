package party.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import face.party.FunBean;
import party.dao.FunDao;
import party.dao.domain.Fun;

/**
 * 功能
 * @author Blucezhang
 *
 */
@EnableAutoConfiguration  
@RestController
public class FunAction {
	
	@Autowired
	private FunDao funDao = null;
	
	/**
	 * 添加功能
	 * @param funBean
	 */
	@RequestMapping(value="/Fun",method=RequestMethod.PUT)
	@ResponseBody
	public void createFunSet(@RequestBody FunBean funBean){
		Fun fun = new Fun();
		fun.setName(funBean.getName());
		fun.setNote(funBean.getNote());
		fun.setConfig(funBean.getConfig());
		fun.setDicConfig(funBean.getDicconfig());
		fun.setBizTypeId(funBean.getBiztypId());
		funDao.save(fun);
		funDao.createRelationShipOfFun(fun.getFunctionId());
	}
	
	/**
	 * 查询所有的功能
	 * @return
	 */
	@RequestMapping(value="/Fun" ,method=RequestMethod.GET)
	@ResponseBody
	public List<Fun> getAllFun(){
		Map<String,Object> result = new HashMap<String, Object>();
		List<Fun> funList = funDao.queryAllFun();
		return funList;
	}
	
	/**
	 * 根据Id删除功能
	 * @param Id
	 */	
	@RequestMapping(value="/Fun/{id}",method=RequestMethod.DELETE)
	@ResponseBody
	public void deleteFunById(@PathVariable Long Id){
		funDao.deleteFun(Id);
	}
	
	/**
	 * 根据Id查询功能的详细信息
	 * @param id
	 * @return
	 */
	@RequestMapping(value="/Fun/{functionId}",method=RequestMethod.GET)
	public Fun getInforbyId(@PathVariable Long functionId){
		Map<String,Object> result = new HashMap<String,Object>();
		Fun fun = funDao.queryOneFunbyId(functionId);
		return fun;
	}
	
	/**
	 * 根据Id修改功能信息
	 * @param id
	 * @param funBean
	 */
	@RequestMapping(value="/Fun/{id}",method=RequestMethod.POST)
	@ResponseBody
	public void updataFunById(@PathVariable Long id,@RequestBody FunBean funBean){
		Fun fun = funDao.getFromId(Integer.parseInt(id.toString()));
		fun.setName(funBean.getName());
		fun.setNote(funBean.getNote());
		fun.setDicConfig(funBean.getDicconfig());
		fun.setConfig(funBean.getConfig());
		fun.setBizTypeId(funBean.getBiztypId());
		funDao.save(fun);
	}
	
	

}
