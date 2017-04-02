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

import face.party.LoginUserBean;
import face.party.UserBean;
import party.dao.LoginUserDao;
import party.dao.domain.LoginUser;
import party.util.Util;

/**
 * 系统用户
 * 
 * @author Blucezhang
 */

@EnableAutoConfiguration
@RestController
public class LoginUserAction {

	@Autowired
	private LoginUserDao loginUserDao = null;

	
	/**
	 * 用户登录
	 * @param userBean
	 * @return
	 */
	@RequestMapping(value = "/Login", method = RequestMethod.POST)
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

	/**
	 * 根据用户Id查询登录用户的Detail
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/getLoginUser/{loginId}", method = RequestMethod.GET)
	public LoginUser getDetail(@PathVariable Long loginId) {
		Map<String, Object> result = new HashMap<String, Object>();
		LoginUser loginuser = (LoginUser) loginUserDao.getLoginUser(loginId);
		return loginuser;
	}

	/**
	 * 修改密码
	 * @param userBean
	 * @return
	 */
	@RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
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

	/**
	 * 查询所有LoginUser用户
	 * 
	 * @return
	 */
	@RequestMapping(value = "/LoginUser", method = RequestMethod.GET)
	@ResponseBody
	public List<LoginUser> queryAllLoginUser() {
		Map<String, Object> result = new HashMap<String, Object>();
		List<LoginUser> LoginUserList = loginUserDao.queryAllLoginUser();
		return LoginUserList;
	}

	/**
	 * 创建LoginUser
	 * 
	 * @param userBaen
	 */
	@RequestMapping(value = "/LoginUser", method = RequestMethod.PUT)
	@ResponseBody
	public void CreateLoginUser(@RequestBody LoginUserBean userBaen) {

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

	/**
	 * 根据Id修改LoginUser
	 * 
	 * @param loginUserBean
	 * @param id
	 */
	@RequestMapping(value = "/LoginUser/{id}", method = RequestMethod.POST)
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

	/**
	 * 根据ID删除系统用户信息
	 * 
	 * @param id
	 */
	@RequestMapping(value = "/LoginUser/{id}", method = RequestMethod.DELETE)
	public void deleteLoginUser(@PathVariable Integer id) {
		loginUserDao.deleteLoginUser(id);
	}

}
