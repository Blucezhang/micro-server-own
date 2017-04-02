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

import face.party.PersonBean;
import party.dao.LoginUserDao;
import party.dao.OrgDao;
import party.dao.PersonDao;
import party.dao.domain.LoginUser;
import party.dao.domain.Person;

@EnableAutoConfiguration  
@RestController  
public class PerAcion {

	
	@Autowired
	private PersonDao personDao = null;
	
	@Autowired
	private OrgDao orgDao = null;
	
	@Autowired
	private LoginUserDao loginUserDao = null;
	
	/**
	 * 查询所有的person用户
	 * @param id
	 * @return map
	 */
	@ResponseBody
	@RequestMapping(value="/Person",method={RequestMethod.GET})
	public List<Person> queryAllPerson(){
		List<Person>  persons = personDao.queryPerson();
 		return persons;
 	}
	
	/**
	 * 根据id查询person信息
	 * @param id
	 * @return map
	 */
	@ResponseBody
	@RequestMapping(value="/Person/{personid}",method={RequestMethod.GET})
	public Person getPerson(@PathVariable Integer personid){
		Person person = (Person) personDao.getFromId(personid);
 		return person;
 	}
	
	/**
	 * 创建person信息
	 * @param person
	 */
	@ResponseBody
	@RequestMapping(value="/Person/Reg",method={RequestMethod.PUT})
	public void createPerson(@RequestBody PersonBean personBean){
		
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
	
	/**
	 * 根据id修改person信息
	 * @param id  //personId
	 */
	@ResponseBody
	@RequestMapping(value="/Person/{id}",method={RequestMethod.POST})
	public Map updPerson(@PathVariable Integer id,@RequestBody PersonBean pb){
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
	
	
	/**
	 * 根据id删除person信息
	 * @param id
	 */
	@ResponseBody
	@RequestMapping(value="/Person/{id}",method={RequestMethod.DELETE})
	public void deletePerson(@PathVariable Integer id){
		personDao.deletePerson(id);
 	}

}