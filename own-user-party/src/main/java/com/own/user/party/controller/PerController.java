package com.own.user.party.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.own.face.party.PersonBean;
import com.own.face.util.base.BaseController;
import com.own.user.party.dao.LoginUserDao;
import com.own.user.party.dao.OrgDao;
import com.own.user.party.dao.PersonDao;
import com.own.user.party.dao.domain.LoginUser;
import com.own.user.party.dao.domain.Person;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/per")
public class PerController extends BaseController {

	@Autowired
	private PersonDao personDao = null;
	@Autowired
	private OrgDao orgDao = null;
	@Autowired
	private LoginUserDao loginUserDao = null;

	@ApiOperation(value = "查询所有的person用户")
	@GetMapping("/Person")
	public@ResponseBody List<Person> queryAllPerson(){
		List<Person>  persons = personDao.queryPerson();
 		return persons;
 	}

	@ApiOperation(value = "根据id查询person信息")
	@GetMapping("/Person/{personid}")
	public @ResponseBody Person getPerson(@PathVariable Integer personid){
		Person person = (Person) personDao.getFromId(personid);
 		return person;
 	}

	@ApiOperation(value = "创建person信息")
	@PutMapping("/Person/Reg")
	public @ResponseBody void createPerson(@RequestBody PersonBean personBean){
		Person person = new Person();
		person.setName(personBean.getName());
		person.setAge(personBean.getAge());
		person.setEmail(personBean.getEmail());
		person.setPhone(personBean.getPhone());
		person.setShipaddres(personBean.getShipaddress());
		person.setPartyTypeId(1);
		person.setPartyTypeName("个人用户");
		personDao.save(person);
		personDao.createRelationShipData(person.getId());
		LoginUser loginUser = new LoginUser();
		loginUser.setLoginName(personBean.getLoginUserName());
		loginUser.setName(personBean.getName());
		loginUser.setPartyId(person.getId());
		loginUser.setPassword(personBean.getPassword());
		loginUserDao.save(loginUser);
		loginUserDao.createRelationShipWithLoginUser(loginUser.getLoginUserId());
		loginUserDao.createRelationShipPersonWithLoginUser(person.getId(), loginUser.getLoginUserId());
		
 	}

	@ApiOperation(value = "根据id修改person信息")
	@PostMapping("/Person/{id}")
	public @ResponseBody Map updPerson(@PathVariable Integer id,@RequestBody PersonBean pb){
		Person person = (Person) personDao.getFromId(id);
		if(pb.getEmail()!=null)
		person.setEmail(pb.getEmail());
		if(pb.getName()!=null)
		person.setName(pb.getName());
		if(pb.getAge()!=null)
		person.setAge(pb.getAge());
		if(pb.getPhone()!=null)
		person.setPhone(pb.getPhone());
		if(pb.getShipaddress()!=null)
		person.setShipaddres(pb.getShipaddress());
		Map map=new HashMap<>();
		map.put("person",  personDao.save(person));
		return map;
 	}

	@ApiOperation(value = "根据id删除person信息")
	@DeleteMapping("/Person/{id}")
	public @ResponseBody void deletePerson(@PathVariable Integer id){
		personDao.deletePerson(id);
 	}

}