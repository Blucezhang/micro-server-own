import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;



public class LoanTest extends TestBase {

	// =========================产品购买测试============================================
	@Test
	public void productTrans() throws Throwable {

		// 		prodTransTest();//账户事务原子交易测试
		// 		transFeeTest();//含手续费原子交易的流程测试
		//		transFeeAndAccProdTest();//含手续费和账号产品原子交易的流程测试
		//		updateProdAttrTest();//含修改产品属性原子交易的流程测试
//		searchAllAccOfPartyTest();//查询某当事人的所有账户
//		accountTransactionAction();//
//		comCusRegister();//添加企业注册信息
//		comExInfoAdd();//添加扩展信息
//		comExInfoUpdate();//修改扩展信息
//		updatePerson();//修改个人基本信息
		
//		queryPerCompany();//
		
	}
	/**
	 * P2P-Buy
	 */
	/*@Test
	public void atomTest() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("atomTest");
		Map map = new HashMap();
		Map personMap = new HashMap();
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	*//**
	 * 将旧的原子交易配置数据转换成新的格式
	 *//*
	@Test
	public void convertOldDataToNew() throws Exception {

		String result = mockMvc
				.perform(get("/System/AtomDataChange").contentType(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk()).andReturn().getResponse()
						.getContentAsString();

		this.printResult(result);

	}
	*//**
	 * P2P-Buy
	 *//*
	@Test
	public void P2PBuy() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("P2P-Buy");
		Map map = new HashMap();
		Map personMap = new HashMap();
		map.put("prodId", 117);
		map.put("planAmt", 2000);
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	@Test
	public void searchLoginUser() throws Throwable{

		LoginUserSearchBean loginUserSearchBean = new LoginUserSearchBean();
		loginUserSearchBean.setPageNo(0);
		loginUserSearchBean.setPageSize(10);
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(loginUserSearchBean); 

		postTest("/System/LoginUser", jsonStr);;
	}
	@Test
	public void getLoginUser() throws Throwable{

		String result = mockMvc
				.perform(get("/System/LoginUser/14").contentType(MediaType.APPLICATION_JSON)
						)
						.andExpect(status().isOk()).andReturn().getResponse()
						.getContentAsString();

		this.printResult(result);
	}
	*//**
	 * 查询机构
	 *//*
	@Test
	public void searchPartyOrg() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("searchPartyOrg");
		Map map = new HashMap();
		Map paramMap = new HashMap();
		paramMap.put("roleTypeId", 6);
		paramMap.put("roleReType", 4);
		paramMap.put("rePartyId", 60);
		paramMap.put("pageNo", 0);
		paramMap.put("pageSize", 10);
		
		map.put("search", paramMap);
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 
		putTest("/Party", jsonStr);;
	}
	*//**
	 * 发标
	 *//*
	@Test
	public void loanIsseu() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("5");
		//invest.setLoginName("test2");
		invest.setToken("4563510100");
		invest.setActionName("issue");
		invest.setProdId(163);
		Map map = new HashMap();
		Map personMap = new HashMap();
		map.put("prodId", 163);
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Transaction/163", jsonStr);;
	}
	*//**
	 * 借款产品列表查询
	 *//*
	@Test
	public void loanProdList() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("getMyLoan");
		Map map = new HashMap();
		Map personMap = new HashMap();
		map.put("templateId", 146);
		map.put("isMy", 1);
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);;
	}
	*//**
	 * 查询个人企业扩展信息
	 *//*
	private void queryPerCompany() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("queryPerCompany");
		Map map = new HashMap();
		Map personMap = new HashMap();
		map.put("partyId", 65);
		map.put("recName", "PerCompany");
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Party", jsonStr);;
	}
	*//**
	 * 修改个人基本信息
	 *//*
	private void updatePerson() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("updatePerson");
		Map map = new HashMap();
		Map personMap = new HashMap();
		personMap.put("industry", "1");
		personMap.put("isGroupCust", "2");
		personMap.put("partyId", 65);
		personMap.put("area", "1");
		
		map.put("partyId", 65);
		map.put("person", personMap);
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Party", jsonStr);;
	}
	*//**
	 * 添加扩展信息
	 *//*
	private void comExInfoAdd() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("comExInfoAdd");
		Map map = new HashMap();
		Map exInfoMap = new HashMap();
		exInfoMap.put("industry", "2");
		exInfoMap.put("area", "1");
		exInfoMap.put("partyId", 35);
		map.put("partyId", 35);
		map.put("exInfo", exInfoMap);
		map.put("recName", "1");
		
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Party", jsonStr);;
	}
	*//**
	 * 修改扩展信息
	 *//*
	private void comExInfoUpdate() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("comExInfoUpdate");
		Map map = new HashMap();
		Map exInfoMap = new HashMap();
		exInfoMap.put("area", "2");
		exInfoMap.put("compNature", "1");
		exInfoMap.put("haveBrand", "2");
		exInfoMap.put("recordId", "2");
		

		map.put("exInfo", exInfoMap);
		map.put("partyId", 35);
		map.put("recordId", 409);
		map.put("recName", "classifyInfo");
		
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Party", jsonStr);;
	}
	*//**
	 * 添加企业注册用户
	 *//*
	private void comCusRegister() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("comCusRegister");
		Map map = new HashMap();
		PartyOrg partyOrg = new PartyOrg();
		Map orgMap = new HashMap();
		orgMap.put("name", "123");
//		orgMap.put("offAddr", "北京市朝阳区朝外大街人寿大厦");
//		orgMap.put("engName", "sdfsdf");
		orgMap.put("parTypeId", 1);
//		partyOrg.setName("企业测试2.0");
//		partyOrg.setOffAddr("北京市朝阳区朝外大街人寿大厦");
//		partyOrg.setEngName("sdfsdf");
//		partyOrg.setParTypeId(1);//组织
		map.put("party", orgMap);
		map.put("roleTypeId", 1);
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Party", jsonStr);;
	}
	*//**
	 * 查询某当事人的所有账户
	 *//*
	private void accountTransactionAction() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("searchAllAccOfParty");
//		Map map = new HashMap();
//		map.put("SubProcess", "测试2");
//		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Account", jsonStr);;
	}
	*//**
	 * 查询某当事人的所有账户
	 *//*
	private void searchAllAccOfPartyTest() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		invest.setProdId(104);
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("searchAllAccOfParty");
//		Map map = new HashMap();
//		map.put("SubProcess", "测试2");
//		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Transaction/104", jsonStr);;
	}
	*//**
	 * 含修改产品属性原子交易的流程测试
	 *//*
	private void updateProdAttrTest() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		invest.setProdId(94);
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("testUpdateProdAttr");
		Map map = new HashMap();
		map.put("TotLimitAmount", "302000");
		map.put("feeFormul", "$abc*0.3+1");
		map.put("SubProcess", "测试2");
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Transaction/94", jsonStr);;
	}
	*//**
	 * 含手续费和账号产品原子交易的流程测试
	 *//*
	private void transFeeAndAccProdTest() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		invest.setProdId(93);
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("transactionFee");
		Map map = new HashMap();
		map.put("amount", "302000");
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Transaction/93", jsonStr);;
	}
	*//**
	 * 手续费测试
	 *//*
	private void transFeeTest() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		invest.setProdId(92);
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("transactionFee");
		Map map = new HashMap();
		map.put("amount", "302000");
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Transaction/92", jsonStr);;
	}


	@Test
	public void getLoanList() throws Exception {

		String result = mockMvc
				.perform(get("/Tran/P2p/Loan").contentType(MediaType.APPLICATION_JSON)
						.param("Action", "GetLoanList"))
						.andExpect(status().isOk()).andReturn().getResponse()
						.getContentAsString();

		this.printResult(result);

	}


	*//**
	 * 账户事务测试
	 *//*
	private void prodTransTest() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		invest.setProdId(84);
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("zhanghushiwu");
		Map map = new HashMap();
		map.put("amount", "302000");
		map.put("stateType", "1");
		//			map.put("relType", "1");
		//			map.put("transaction2", "1");
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Transaction/84", jsonStr);;
	}*/
}
