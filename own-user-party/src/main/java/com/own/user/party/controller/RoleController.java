package com.own.user.party.controller;

import java.util.List;

import com.own.face.party.RoleBean;
import com.own.face.util.base.BaseController;
import com.own.user.party.dao.RoleDao;
import com.own.user.party.dao.domain.Role;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

/**
 * 角色
 * @author Blucezhang
 *
 */
@RestController
@RequestMapping("/role")
public class RoleController extends BaseController {

	@Autowired
	private RoleDao roleDao = null;

	/**
	 * 查询所有的Role
	 * @return
	 */
	@RequestMapping(value="/Role",method=RequestMethod.GET)
	public List<Role> getAllRole(){
		List<Role> roleList = roleDao.getAllRole();
		return roleList;
	}

	/**
	 * 创建Role
	 * @param roleBean
	 */
	@RequestMapping(value="/Role",method=RequestMethod.PUT)
	public void createRole( @RequestBody RoleBean roleBean){
		Role role = new Role();
		role.setName(roleBean.getName());
		role.setNote(roleBean.getNote());
		if(roleBean.getOrgId()!=null)
			role.setOrgId(roleBean.getOrgId());
		role.setPartmentId(roleBean.getPartmentId());
		roleDao.save(role);

		roleDao.createRelationShipWithRole(role.getRole());

		//添加功能
		List<Long> funidList = roleBean.getFunIdsList();
		Long roleId = role.getRole();
		if(funidList.size()>0){
			for (Long funId:funidList) {
				roleDao.createRelationShipRoleAndFun(roleId, funId);
			}
		}
		//授权
		roleDao.createRelationShipRoleAndOrg(role.getRole(),roleBean.getPartmentId());
	}

	/**
	 * 根据Id修改Role
	 * @param id
	 * @param roleBean
	 */
	@RequestMapping(value="/Role/{role}",method=RequestMethod.POST)
	public void updateRole(@PathVariable Long id,@RequestBody RoleBean roleBean){
		roleDao.deleteRoleAndFunRelationShip(id);
		roleDao.deleteRoleAndOrgRelationShip(id);
		Role role = roleDao.getFromId(Integer.parseInt(id.toString()));
		role.setName(roleBean.getName());
		role.setNote(roleBean.getNote());
		if(roleBean.getOrgId()!=null)
			role.setOrgId(roleBean.getOrgId());
		role.setPartmentId(roleBean.getPartmentId());
		roleDao.save(role);

		//添加功能
		List<Long> funidList = roleBean.getFunIdsList();
		Long roleId = role.getRole();
		if(funidList.size()>0){
			for (Long funId:funidList) {
				roleDao.createRelationShipRoleAndFun(roleId,funId);
			}
		}

		//授权
		roleDao.createRelationShipRoleAndOrg(role.getRole(),roleBean.getPartmentId());
	}

	/**
	 * 根据RoleId删除Role信息、关系
	 * @param id
	 */
	@RequestMapping(value="/Role/{id}",method=RequestMethod.DELETE)
	public void delete(@PathVariable Long id){
		roleDao.deleteRoleAndFunRelationShip(id);
		roleDao.deleteRoleAndOrgRelationShip(id);
		roleDao.deleteFun(id);
	}

	/**
	 * 角色添加功能
	 * funids roleid
	 * @param params
	 */
	@RequestMapping(value="/Role/addFun",method=RequestMethod.PUT)
	public void createRelationShipRoleAndFun(@RequestBody RoleBean roleBean){

	}
	
}
