package com.own.face.party;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.face.core.FaceBase;
import com.own.face.core.IfException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;


@Slf4j
@Service
public class PartyFace extends FaceBase {

	protected String serviceUrl = "//PARTY/";

	/**
	 * 根据PersonId查询用户的常用信息
	 * 
	 * @param personBean
	 * @return
	 */
	public Map<String, Object> getPersonById(PersonBean personBean) {
		Map<String, Object> result = get(serviceUrl+ "/Person/{personid}", Map.class, personBean);
		return result;
	}

	/**
	 * 查询所有的Person
	 * @return
	 */
	public List getAllPerson() {
		List allPerson = get(serviceUrl+ "/Person", List.class, "");
		return allPerson;
	}

	/**
	 * 根据用户Id删除Person信息
	 * @param id
	 */
	public void DeletePerById(Long id){
		restTemplate.delete(serviceUrl+"/Person/{id}", id);
	}
	
	
	public Orgnization getOrgById(OrgBean orgBean) throws IfException {
		Orgnization orgnization = get(serviceUrl+ "/Org/{parOrgId}", Orgnization.class, orgBean);// 注意org的大小写问题，经调试一会大写可行，一会小写可行
		if (orgnization != null) {
			System.out.println("调用接口返回结果：id:" + orgnization.getId()
					+ "   name:" + orgnization.getName() + "  level:"
					+ orgnization.getLevel());
		} else {
			System.out.println("PartyFace result is null !");
		}
		String str = get(serviceUrl + "/Org/msg",String.class, orgBean);
		System.out.println("string:" + str);

		if (orgnization == null) {
			throw new IfException(CAN_NOT_FIND_ORGNIZATION, "找不到对应的组织！！！");
		} else {
			return orgnization;
		}

	}

	/**
	 * 修改用户基本信息
	 * @param personBean
	 * @return
	 * @throws JsonProcessingException 
	 */
	public Map<String, Object> UpdatePerson(PersonBean personBean) {
		Map<String,Object> result = post(serviceUrl, personBean, Map.class, null);
		return result;
	}
	
	/**
	 * 登录验证
	 * @param loginUserBean
	 * @return
	 * @throws JsonProcessingException 
	 */
	public Map<String, Object> Login(LoginUserBean loginUserBean){
		Map<String,Object> result = post(serviceUrl+"/Login", loginUserBean, Map.class, new HashMap());
		return result;
	}

	/**
	 * 个人用户注册
	 * @param personBean
	 * @return
	 * @throws JsonProcessingException 
	 */
	public PersonBean Registered(PersonBean personBean){
		return put(serviceUrl + "/Person/Reg",personBean,PersonBean.class);
	}

	
	/**
	 * 根据Id获取LoginUser详细信息
	 * @param loginUserBean
	 * @return
	 */
	public Map<String, Object> LoginUserDetail(LoginUserBean loginUserBean) {
		Map<String, Object> result = get(serviceUrl + "/getLoginUser/{loginId}", Map.class, loginUserBean);
		return result;
	}

	/**
	 * 查询所有用户信息
	 * 
	 * @return
	 */
	public List queryAllLoginUser() {
		List result = restTemplate.getForObject(serviceUrl + "/LoginUser",List.class, "");
		return result;
	}

	/**
	 * 创建（关联）登录用户(企业)
	 * 
	 * @param loginUserBean
	 * @return
	 */
	public LoginUserBean CreateLoginUser(LoginUserBean loginUserBean) {
		return put(serviceUrl + "/LoginUser", loginUserBean,LoginUserBean.class);
	}

	/**
	 * 根据LoginUserName修改密码
	 * 
	 * @param userBean
	 */
	public void updataPassword(UserBean userBean) {
		post(serviceUrl + "/updatePassword", userBean, null, null);
	}

	/**
	 * 创建功能
	 * 
	 * @param funbean
	 * @return
	 */
	public FunBean createFuntion(FunBean funbean) {
		return put(serviceUrl + "/Fun", funbean,FunBean.class);
	}

	/**
	 * 获取所有的功能
	 * 
	 * @return
	 */
	public List queryAllFun() {
		List result = get(serviceUrl + "/Fun",List.class, "");
		return result;
	}

	/**
	 * 根据ID获取功能的详细信息
	 * 
	 * @return
	 */
	public Map<String, Object> getFunInfor(FunBean funBean) {
		Map<String, Object> result = get(serviceUrl+ "/Fun/{functionId}", Map.class, funBean);
		return result;
	}

	/**
	 * 根据ID修改FunBean
	 * 
	 * @param funBean
	 */
	public Object updateFun(FunBean funBean) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("functionId", funBean.getFunctionId());
		Object obj = post(serviceUrl+"/Fun/{functionId}", funBean, Object.class, params);
		return obj;
	}

	/**
	 * 查询所有的权限
	 * 
	 * @return
	 */
	public List queryAllRole() {
		List result = restTemplate.getForObject(serviceUrl + "/Role", List.class, "null");
		return result;
	}

	/**
	 * 创建角色
	 * 
	 * @param roleBean
	 * @return
	 */
	public RoleBean createRole(RoleBean roleBean) {
		return put(serviceUrl + "/Role", roleBean,RoleBean.class);
		
	}

	/**
	 * 根据ID修改角色信息
	 * 
	 * @param roleBean
	 * @return
	 */
	public Map<String, Object> updataRole(RoleBean roleBean) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("role", roleBean.getRole());
		Map<String,Object> result = post(serviceUrl+"/Role/{role}", roleBean, Map.class, params);
		return result;
	}
}
