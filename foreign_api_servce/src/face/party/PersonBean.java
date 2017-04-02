package face.party;

import java.util.List;

public class PersonBean {

	public PersonBean(){
		
	}
	
	private Integer age;

	private String email;

	private String phone;

	private Long partmentId;

	private Long orgId;

	private String name;

	private String loginUserName;

	private String password;

	private Long personid;
	
	private List<String> shipaddress;
	
	private Integer flag ; //判断是买家还是卖家  0是买家，1是卖家
	

	public Long getPersonid() {
		return personid;
	}

	public void setPersonid(Long personid) {
		this.personid = personid;
	}

	public List<String> getShipaddress() {
		return shipaddress;
	}

	public void setShipaddress(List<String> shipaddress) {
		this.shipaddress = shipaddress;
	}

	public Integer getFlag() {
		return flag;
	}

	public void setFlag(Integer flag) {
		this.flag = flag;
	}

	public String getLoginUserName() {
		return loginUserName;
	}

	public void setLoginUserName(String loginUserName) {
		this.loginUserName = loginUserName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Long getPartmentId() {
		return partmentId;
	}

	public void setPartmentId(Long partmentId) {
		this.partmentId = partmentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

}
