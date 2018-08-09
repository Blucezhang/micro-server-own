package com.own.user.party.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.face.party.LoginUserBean;
import com.own.face.party.UserBean;
import com.own.face.util.Util;
import com.own.face.util.base.BaseController;
import com.own.user.party.dao.LoginUserDao;
import com.own.user.party.dao.domain.LoginUser;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统用户
 * 
 * @author Blucezhang
 */

@RestController
@RequestMapping("/login")
public class LoginUserController extends BaseController {

	@Autowired
	private LoginUserDao loginUserDao = null;

	@ApiOperation(value = " 用户登录")
	@PostMapping("/Login")
	public Map<String,Object> login(@RequestBody UserBean userBean) {
		Map result  = new HashMap();
		String name = Util.toStringAndTrim(userBean.getLoginUserName());
		String password = Util.toStringAndTrim(userBean.getPassword());
		LoginUser loginuser = new LoginUser();
		List lists = loginUserDao.getDetailOfLoginUser(name,password);

		if (lists.size()<=0) {
			result.put("message", "teller_does_not_exist");
			return result;
		}
		result.put("LoginUser", lists.get(0));
		result.put("person", lists.get(1));
		return result;
		
	}

	@ApiOperation(value = "根据用户Id查询登录用户的Detail")
	@GetMapping("/getLoginUser/{loginId}")
	public LoginUser getDetail(@PathVariable Long loginId) {
		Map<String, Object> result = new HashMap<String, Object>();
		LoginUser loginuser = (LoginUser) loginUserDao.getLoginUser(loginId);
		return loginuser;
	}

	@ApiOperation(value = "修改密码")
	@PostMapping("/updatePassword")
	public Map<String, Object> updatePassword(@RequestBody UserBean userBean) {
		Map<String, Object> result = new HashMap<String, Object>();
		String loginName = userBean.getLoginUserName();
		String password = userBean.getPassword();
		LoginUser loginUser = new LoginUser();
		loginUser = (LoginUser) loginUserDao.getDetailOfLoginUser(loginName,
				password);
		if (loginUser == null)
			result.put("message", "the user not exit");
		else {
			loginUser = (LoginUser) loginUserDao.updatepassword(
					userBean.getLoginUserName(), userBean.getPassword(),
					userBean.getNewpassword());
			result.put("loginUser", loginUser);
		}
		return result;
	}

	@ApiOperation(value = "查询所有LoginUser用户")
	@GetMapping("/LoginUser")
	public @ResponseBody List<LoginUser> queryAllLoginUser() {
		Map<String, Object> result = new HashMap<String, Object>();
		List<LoginUser> LoginUserList = loginUserDao.queryAllLoginUser();
		return LoginUserList;
	}

	@ApiOperation(value = "创建LoginUser")
	@PutMapping("/LoginUser")
	public @ResponseBody void CreateLoginUser(@RequestBody LoginUserBean userBaen) {
		LoginUser user = new LoginUser();
		user.setName(userBaen.getName());
		user.setEmail(userBaen.getEmail());
		user.setLoginName(userBaen.getLoginUserName());
		user.setPhone(userBaen.getPhone());
		user.setPassword(userBaen.getPassword());
		loginUserDao.save(user);
		// 用户登录信息、详细信息、都放在LoginUser节点中
		loginUserDao.createRelationShipWithLoginUser(user.getLoginUserId());
		if (userBaen.getPartmentId() != null) {
			loginUserDao.createRelationShipLoginUserAndOrg(
					user.getLoginUserId(), userBaen.getPartmentId());
		} else {
			loginUserDao.createRelationShipLoginUserAndOrg(
					user.getLoginUserId(), userBaen.getOrgId());
		}
	}

	@ApiOperation(value = "根据Id修改LoginUser")
	@PostMapping("/LoginUser/{id}")
	public void updateLoginUser(@RequestBody LoginUserBean loginUserBean,
			@PathVariable Long id) {
		loginUserDao.deleteLoginUserAndOrgs(id);
		LoginUser user = new LoginUser();
		user.setName(loginUserBean.getName());
		user.setEmail(loginUserBean.getEmail());
		user.setLoginName(loginUserBean.getLoginUserName());
		user.setName(loginUserBean.getName());
		user.setPhone(loginUserBean.getPhone());
		if (loginUserBean.getPartmentId() != null)
			user.setPartmentId(loginUserBean.getPartmentId());
		user.setOrgId(loginUserBean.getOrgId());
		loginUserDao.save(user);

		if (loginUserBean.getPartmentId() != null) {
			loginUserDao.createRelationShipLoginUserAndOrg(
					user.getLoginUserId(), loginUserBean.getPartmentId());
		} else {
			loginUserDao.createRelationShipLoginUserAndOrg(
					user.getLoginUserId(), loginUserBean.getOrgId());
		}
	}


	@ApiOperation(value = "根据ID删除系统用户信息")
	@DeleteMapping("/LoginUser/{id}")
	public void deleteLoginUser(@PathVariable Integer id) {
		loginUserDao.deleteLoginUser(id);
	}

}
