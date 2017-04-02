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

import face.party.OrgBean;
import face.party.Orgnization;
import party.dao.LoginUserDao;
import party.dao.OrgDao;
import party.dao.domain.Organization;
import party.util.Util;
 

@EnableAutoConfiguration  
@RestController
public class OrgAction {
	
	@Autowired
	private OrgDao orgDao = null;
	
	@Autowired
	private LoginUserDao loginUserDao = null;
	
	
	/**
	 * 根据id查询organization信息
	 * @param id
	 * @return map
	 */
	@ResponseBody
	@RequestMapping(value="/Org",method={RequestMethod.GET})
	public Map<String,Object> queryOrg(){
		List<Organization> organizations = orgDao.queryOrg();
		Map<String,Object> map = new HashMap();
		map.put("result", organizations);
 		return map;
 	}
	
	/**
	 * 根据id查询organization信息
	 * @param id
	 * @return map
	 */
	@ResponseBody
	@RequestMapping(value="/Org",method={RequestMethod.GET},params={"Action=All"})
	public Map<String,Object> queryAllOrg(){
		List<Organization> organizations = orgDao.queryAllOrg();
		Map<String,Object> map = new HashMap();
		map.put("result", organizations);
 		return map;
 	}
	
	/**
	 * 根据id查询organization信息
	 * @param id
	 * @return map
	 */
	@ResponseBody
	@RequestMapping(value="/Org/{id}",method={RequestMethod.GET},params={"Action=Single"})
	public Organization getOrgSingle(@PathVariable Integer id){
		Organization organization = (Organization) orgDao.getFromId(id);
 		return organization;
 	}
	
	/**
	 * 根据id查询organization信息
	 * @param id
	 * @return map
	 */
	@ResponseBody
	@RequestMapping(value="/Org/{id}",method={RequestMethod.GET},params={"Action=All"})
	public List<Organization> getOrgAll(@PathVariable Integer id){
		List<Organization> organizations = orgDao.queryChildOrg(id);
 		return organizations;
 	}
	
	/**
	 * 创建organization信息 (创建组织)
	 * @param organization
	 */
	@ResponseBody
	@RequestMapping(value="/Org",method={RequestMethod.PUT})
	public void createOrg(@RequestBody OrgBean ob){
		
		Organization organization = new Organization();
		organization.setName(ob.getName());
		organization.setLoginName(ob.getLoginName());
		organization.setLevel(ob.getLevel());
		organization.setPartyTypeId(1);
		organization.setPartyTypeName("组织");
		organization.setPhone(ob.getPhone());
		organization.setEmail(ob.getEmail());
		orgDao.save(organization);
		if(Util.isNullOrEmpty(organization.getOrgId())){
			orgDao.createRelationShipData(organization.getId());
		}
 	}
	
	/**
	 * 创建organization子级信息
	 * @param organization
	 */
	@ResponseBody
	@RequestMapping(value="/Org/{id}",method={RequestMethod.PUT})
	public void createOrgChild(@PathVariable Integer id,@RequestBody Organization organization){
		organization.setPartyTypeId(2);
		organization.setPartyTypeName("部门");
		orgDao.save(organization);
		//System.out.println(o.getId()+","+o.getName());
		orgDao.createChildRelationShip(id,organization.getId());
		
 	}
	
	/**
	 * 根据id修改org信息
	 * @param id
	 */
	@ResponseBody
	@RequestMapping(value="/Org/{id}",method={RequestMethod.POST})
	public void updOrg(@PathVariable Integer id,@RequestBody Organization o){
		Organization organization = (Organization) orgDao.getFromId(id);
		organization.setName(o.getName());
		organization.setLevel(o.getLevel());
		orgDao.save(organization);
 	}
	
	/**
	 * 根据id删除org信息
	 * @param id
	 */
	@ResponseBody
	@RequestMapping(value="/Org/{id}",method={RequestMethod.DELETE})
	public void deleteOrg(@PathVariable Integer id){
		orgDao.deleteOrg(id);
 	}

}
