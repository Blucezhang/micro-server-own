import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PartyActionTest extends TestBase {

	/*//添加企业
	/*@Test
	public void addOrgTest() throws Exception {

		//汇金贷当事人信息
		PartyOrgBean partyOrgBean = new PartyOrgBean();
		partyOrgBean.setParTypeId(1);
		partyOrgBean.setName("北京汇金贷资产管理有限公司20150119");
		partyOrgBean.setNote("北京汇金贷资产管理有限公司20150119");
		partyOrgBean.setClertNum(1);
		partyOrgBean.setAccBank("中国银行");
		partyOrgBean.setEngName("cigooo");
		partyOrgBean.setCreateDay(new Date(System.currentTimeMillis()));
		partyOrgBean.setIsGroup(1);
		partyOrgBean.setLicEndDay(new Date(System.currentTimeMillis()));
		partyOrgBean.setLoanCardNo("6222020200002929292");
		partyOrgBean.setLocalTaxNo("201302020042");
		partyOrgBean.setOffAddr("北京市朝阳区朝外大街人寿大厦1202 20150119");
		partyOrgBean.setState((short)0);
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(partyOrgBean);
		
		printResult(mockMvc.perform(put("/Party/Org")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//修改企业
	@Test
	public void updateOrgTest() throws Exception {

//		HashMap<String,Object> partyOrgInfo = new HashMap<String,Object>();
//		partyOrgInfo.put("name","北京汇金贷资产管理有限公司  update test2");
//		partyOrgInfo.put("note","北京汇金贷资产管理有限公司  update test2");
//		partyOrgInfo.put("engName","cigooo update test");
//		partyOrgInfo.put("offAddr","北京市朝阳区朝外大街人寿大厦1202 update test");
		
		PartyOrgBean partyOrgBean = new PartyOrgBean();
		partyOrgBean.setPartyId(31L);
		partyOrgBean.setName("北京汇金贷资产管理有限公司20150119  update");//名称
		partyOrgBean.setIsGroup(1);//集团公司标志
		partyOrgBean.setIsIpo(1);//上市公司标志
		partyOrgBean.setOffAddr("北京市朝阳区朝外大街人寿大厦1202 20150119 update");//办公地址
		partyOrgBean.setComType(1);//公司类型
		partyOrgBean.setRegType(2);//注册类型
		partyOrgBean.setOrgCode("0102232323");//组织机构代码
		partyOrgBean.setShortName("汇金贷");//简称
		partyOrgBean.setEngName("cigooo");//英文名称
		partyOrgBean.setRegCapi(new BigDecimal("20000000.00"));//注册资本
//		partyOrgBean.setLicNo(partyOrg.getLicNo());//营业执照号码
		partyOrgBean.setLicStartDay(new Date(System.currentTimeMillis()));//营业执照开始日期
		partyOrgBean.setLicEndDay(new Date(System.currentTimeMillis()));//营业执照到期日期
//		partyOrgBean.setLoanCardNo(partyOrg.getLoanCardNo());//贷款号码
		partyOrgBean.setRegAddr("");//注册地址
//		partyOrgBean.setNatTaxNo(partyOrg.getNatTaxNo());//国税号码
//		partyOrgBean.setLocalTaxNo(partyOrg.getLocalTaxNo());//地税号码
		partyOrgBean.setPhoneNo("400-873-8800");//电话号码
		partyOrgBean.setClertNum(new Integer("10000"));//员工人数
		partyOrgBean.setZip("01010067");//邮编
		partyOrgBean.setOwer("张岩");//所有者
		partyOrgBean.setAccBank("中国银行");//开户行
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(partyOrgBean);

		printResult(mockMvc.perform(post("/Party/Org/31")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonStr)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//获取企业
	@Test
	public void getOrgTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//按条件查找企业信息
	@Test
	public void searchAllOrg() throws Throwable {
		Map paramMap = new HashMap();
		paramMap.put("roleTypeId", "6");//角色类型
//		paramMap.put("rePartyId", "60");
//		paramMap.put("roleReType", "4");//角色关系类型
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(paramMap);
		
		printResult(mockMvc.perform(post("/Party/Org")
				.param("Action", "All")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonStr)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//按条件查找个人信息
	@Test
	public void searchAllPerson() throws Throwable {
		Map paramMap = new HashMap();
		paramMap.put("roleTypeId", "5");//角色类型
//		paramMap.put("rePartyId", "60");
//		paramMap.put("roleReType", "4");//角色关系类型
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(paramMap);
		
		printResult(mockMvc.perform(post("/Party/Per")
				.param("Action", "All")
				.contentType(MediaType.APPLICATION_JSON)
				.content(jsonStr)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	
	//添加分类及管理
	@Test
	public void addOrgClassAndMngTest() throws Exception {

		//汇金贷当事人信息
		OrgClassAndMng orgClassAndMng = new OrgClassAndMng();
		orgClassAndMng.setIndustry("金融业");//所属行业
		orgClassAndMng.setIsGroupCust("1");//是否集团客户
		orgClassAndMng.setIsListedComp("1");//是否上市公司
		orgClassAndMng.setArea("北京");//所属区域
		orgClassAndMng.setIsInnoEnt("1");//是否为创新企业
		orgClassAndMng.setHaveBrand("1");//是否有品牌
		orgClassAndMng.setCompNature("私企");//公司性质
		orgClassAndMng.setRegType("注册类型1");//登记注册类型
		orgClassAndMng.setCustScale("1000万级");//客户规模
		orgClassAndMng.setSource("互联网");//客户来源
		orgClassAndMng.setHaveImpExpRight("1");//是否有进出口经营权

		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgClassAndMng);
		
		printResult(mockMvc.perform(put("/Party/Org/31/OrgClassAndMng")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//修改分类及管理
	@Test
	public void updateOrgClassAndMngTest() throws Exception {

		//汇金贷当事人信息
		OrgClassAndMng orgClassAndMng = new OrgClassAndMng();
		orgClassAndMng.setRecordId(72);//当前记录id
		
		orgClassAndMng.setIndustry("金融业update");//所属行业
		orgClassAndMng.setIsGroupCust("1");//是否集团客户
		orgClassAndMng.setIsListedComp("1");//是否上市公司
		orgClassAndMng.setArea("北京");//所属区域
		orgClassAndMng.setIsInnoEnt("1");//是否为创新企业
		orgClassAndMng.setHaveBrand("1");//是否有品牌
		orgClassAndMng.setCompNature("私企");//公司性质
		orgClassAndMng.setRegType("注册类型1");//登记注册类型
		orgClassAndMng.setCustScale("1000万级");//客户规模
		orgClassAndMng.setSource("互联网");//客户来源
		orgClassAndMng.setHaveImpExpRight("1");//是否有进出口经营权

		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgClassAndMng);
		
		printResult(mockMvc.perform(post("/Party/Org/31/OrgClassAndMng")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取分类及管理
	@Test
	public void getOrgClassAndMngTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgClassAndMng")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//获取就职企业(自然人信息)
	@Test
	public void getPerCompanyTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Per/33/Company")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}

	//添加企业持股人信息
	@Test
	public void addOrgShareholdersTest() throws Exception {

		//企业持股人信息
		OrgShareholders orgShareholders = new OrgShareholders();
		orgShareholders.setHolderName("张岩");//股东名称
		orgShareholders.setHolderType("大股东");//股东类型
		orgShareholders.setIdType("1");//证件类型
		orgShareholders.setIdNum("371324198803029882");//证件号码
		orgShareholders.setHoldPercent(new BigDecimal("0.51"));//持股比例
		orgShareholders.setHoldAmt(new BigDecimal("11000000"));//持股金额
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgShareholders);
		
		printResult(mockMvc.perform(put("/Party/Org/31/OrgShareholders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取企业持股人信息
	@Test
	public void getOrgShareholdersTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgShareholders/73")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//修改企业持股人信息
	@Test
	public void updateOrgShareholdersTest() throws Exception {

		//企业持股人信息
		OrgShareholders orgShareholders = new OrgShareholders();
		orgShareholders.setRecordId(73);
		orgShareholders.setHolderName("张岩update");//股东名称
		orgShareholders.setHolderType("大股东");//股东类型
		orgShareholders.setIdType("1");//证件类型
		orgShareholders.setIdNum("371324198803029882");//证件号码
		orgShareholders.setHoldPercent(new BigDecimal("0.51"));//持股比例
		orgShareholders.setHoldAmt(new BigDecimal("11000000"));//持股金额
		

		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgShareholders);
		
		printResult(mockMvc.perform(post("/Party/Org/31/OrgShareholders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//查找企业持股人信息
	@Test
	public void searchAllOrgShareholdersTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/34/OrgShareholders?Action=All")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//删除企业持股人信息
	@Test
	public void delOrgShareholdersTest() throws Throwable {
		printResult(mockMvc.perform(delete("/Party/Org/31/OrgShareholders/73")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	
	
	//添加企业领导人信息
	@Test
	public void addOrgLeadersTest() throws Exception {

		//企业领导人信息
		OrgLeaders orgLeaders = new OrgLeaders();
		orgLeaders.setLeaderName("张岩");//姓名
		orgLeaders.setIdType("身份证");//证件类型
		orgLeaders.setIdNum("371324198304187881");//证件号码
		orgLeaders.setDefLinkMan("刘XX");//默认联系人
		orgLeaders.setSex("男");//性别
		orgLeaders.setBirthDay("1983-01-01");//出生日期
		orgLeaders.setLeaderType("懂事");//领导类别
		orgLeaders.setEmail("service@cigooo.com");//邮箱
		orgLeaders.setOfficePhone("01058585858");//办公电话
		orgLeaders.setMobile("13902021211");//移动电话
		orgLeaders.setHomePhone("010-8272721");//住宅电话
		orgLeaders.setPosition("董事长");//职务
		orgLeaders.setHighestDegree("高中");//最高学历
		orgLeaders.setFax("58585858");//传真号码
		orgLeaders.setPostCode("100176");//邮政编码
		orgLeaders.setHomeAddr("北京市xx区xx街道xx小区xx楼xx室");//家庭地址
		orgLeaders.setRemark("闲人勿扰");//备注
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgLeaders);
		
		printResult(mockMvc.perform(put("/Party/Org/31/OrgLeaders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取企业领导人信息
	@Test
	public void getOrgLeadersTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgLeaders/75")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//修改企业领导人信息
	@Test
	public void updateOrgLeadersTest() throws Exception {

		//企业领导人信息
		OrgLeaders orgLeaders = new OrgLeaders();
		orgLeaders.setRecordId(75);
		
		orgLeaders.setLeaderName("张岩update");//姓名
		orgLeaders.setIdType("身份证");//证件类型
		orgLeaders.setIdNum("371324198304187881");//证件号码
		orgLeaders.setDefLinkMan("刘XX");//默认联系人
		orgLeaders.setSex("男");//性别
		orgLeaders.setBirthDay("1983-01-01");//出生日期
		orgLeaders.setLeaderType("懂事");//领导类别
		orgLeaders.setEmail("service@cigooo.com");//邮箱
		orgLeaders.setOfficePhone("01058585858");//办公电话
		orgLeaders.setMobile("13902021211");//移动电话
		orgLeaders.setHomePhone("010-8272721");//住宅电话
		orgLeaders.setPosition("董事长");//职务
		orgLeaders.setHighestDegree("高中");//最高学历
		orgLeaders.setFax("58585858");//传真号码
		orgLeaders.setPostCode("100176");//邮政编码
		orgLeaders.setHomeAddr("北京市xx区xx街道xx小区xx楼xx室");//家庭地址
		orgLeaders.setRemark("闲人勿扰");//备注
		

		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgLeaders);
		
		printResult(mockMvc.perform(post("/Party/Org/31/OrgLeaders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//查找企业领导人信息
	@Test
	public void searchAllOrgLeadersTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgLeaders?Action=All")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//删除企业领导人信息
	@Test
	public void delOrgLeadersTest() throws Throwable {
		printResult(mockMvc.perform(delete("/Party/Org/31/OrgLeaders/75")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	
	//==============================企业合作伙伴信息开始=========================================
	//添加企业合作伙伴信息
	@Test
	public void addOrgPartnersTest() throws Exception {

		//企业合作伙伴信息
		OrgPartners orgPartners = new OrgPartners();
		orgPartners.setPartnerName("合作伙伴1");//名称
		orgPartners.setCooperNature("投资");//合作性质
		orgPartners.setYearLimit("10");//年限
		orgPartners.setPayableAmt(new BigDecimal("5000000"));//应付账款
		orgPartners.setTerm("5年");//周期
		orgPartners.setCustScale("500-1000人");//客户规模
		orgPartners.setCustSrc("互联网");//客户来源
		orgPartners.setRemark("企业合作伙伴信息");//备注
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgPartners);
		
		printResult(mockMvc.perform(put("/Party/Org/31/OrgPartners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取企业合作伙伴信息
	@Test
	public void getOrgPartnersTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgPartners/76")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//修改企业合作伙伴信息
	@Test
	public void updateOrgPartnersTest() throws Exception {

		//企业合作伙伴信息
		OrgPartners orgPartners = new OrgPartners();
		orgPartners.setRecordId(76);
		
		orgPartners.setPartnerName("合作伙伴update");//名称
		orgPartners.setCooperNature("投资");//合作性质
		orgPartners.setYearLimit("10");//年限
		orgPartners.setPayableAmt(new BigDecimal("5000000"));//应付账款
		orgPartners.setTerm("5年");//周期
		orgPartners.setCustScale("500-1000人");//客户规模
		orgPartners.setCustSrc("互联网");//客户来源
		orgPartners.setRemark("企业合作伙伴信息update");//备注
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgPartners);
		

		printResult(mockMvc.perform(post("/Party/Org/31/OrgPartners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//查找企业合作伙伴信息
	@Test
	public void searchAllOrgPartnersTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/34/OrgPartners?Action=All")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//删除企业合作伙伴信息
	@Test
	public void delOrgPartnersTest() throws Throwable {
		printResult(mockMvc.perform(delete("/Party/Org/31/OrgPartners/76")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//==============================企业合作伙伴信息结束=========================================	
	
	
	//==============================企业被担保信息开始=========================================	
	//添加企业被担保信息
	@Test
	public void addOrgBeGuaranteeTest() throws Exception {

		//企业被担保信息开始
		OrgBeGuarantee orgBeGuarantee = new OrgBeGuarantee();
		orgBeGuarantee.setGuarContract("HT-01212120");//保证合同编号
		orgBeGuarantee.setState("1");//保证合同有效状态
		orgBeGuarantee.setContractAmt(new BigDecimal("2000000"));//保证合同金额
		orgBeGuarantee.setCurrency("人民币");//保证合同计量币种
		orgBeGuarantee.setFinaInstName("汇金贷");//业务发生金融机构名称

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgBeGuarantee);
		
		printResult(mockMvc.perform(put("/Party/Org/31/OrgBeGuarantee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取企业被担保信息
	@Test
	public void getOrgBeGuaranteeTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgBeGuarantee/78")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//修改企业被担保信息
	@Test
	public void updateOrgBeGuaranteeTest() throws Exception {

		//企业被担保信息
		OrgBeGuarantee orgBeGuarantee = new OrgBeGuarantee();
		orgBeGuarantee.setRecordId(78);
		
		orgBeGuarantee.setGuarContract("HT-01212120");//保证合同编号
		orgBeGuarantee.setState("1");//保证合同有效状态
		orgBeGuarantee.setContractAmt(new BigDecimal("2000000"));//保证合同金额
		orgBeGuarantee.setCurrency("人民币");//保证合同计量币种
		orgBeGuarantee.setFinaInstName("汇金贷update");//业务发生金融机构名称
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgBeGuarantee);
		

		printResult(mockMvc.perform(post("/Party/Org/31/OrgBeGuarantee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//查找企业被担保信息
	@Test
	public void searchAllOrgBeGuaranteeTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgBeGuarantee?Action=All")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//删除企业被担保信息
	@Test
	public void delOrgBeGuaranteeTest() throws Throwable {
		printResult(mockMvc.perform(delete("/Party/Org/31/OrgBeGuarantee/78")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//==============================企业被担保信息结束=========================================
	
	
	//==============================企业经营信息开始=========================================	
	//添加企业经营信息
	@Test
	public void addOrgManagementTest() throws Exception {

		//企业经营信息
		OrgManagement orgManagement = new OrgManagement();
		orgManagement.setIndexType("金额");//指标类型
		orgManagement.setIndexName("流水");//指标名称
		orgManagement.setIndexValue("300000");//指标值
		orgManagement.setTerm("1个月");////时间周期
		orgManagement.setReportDate(new Date(System.currentTimeMillis()));//报告日期
		orgManagement.setRemark("企业经营信息");//描述
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgManagement);
		
		printResult(mockMvc.perform(put("/Party/Org/31/OrgManagement")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取企业经营信息
	@Test
	public void getOrgManagementTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgManagement/81")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//修改企业经营信息
	@Test
	public void updateOrgManagementTest() throws Exception {

		//企业经营信息
		OrgManagement orgManagement = new OrgManagement();
		orgManagement.setRecordId(81);
		
		orgManagement.setIndexType("金额");//指标类型
		orgManagement.setIndexName("流水");//指标名称
		orgManagement.setIndexValue("300000");//指标值
		orgManagement.setTerm("1个月");////时间周期
		orgManagement.setReportDate(new Date(System.currentTimeMillis()));//报告日期
		orgManagement.setRemark("企业经营信息update");//描述
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgManagement);
		

		printResult(mockMvc.perform(post("/Party/Org/31/OrgManagement")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//查找企业经营信息
	@Test
	public void searchAllOrgManagementTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/34/OrgManagement?Action=All")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//删除企业经营信息
	@Test
	public void delOrgManagementTest() throws Throwable {
		printResult(mockMvc.perform(delete("/Party/Org/31/OrgManagement/80")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//==============================企业经营信息结束=========================================
	
	//==============================企业纳税信息开始=========================================	
	//添加企业纳税信息
	@Test
	public void addOrgTaxTest() throws Exception {

		//企业纳税信息
		OrgTax orgTax = new OrgTax();
		orgTax.setTaxDate(new Date(System.currentTimeMillis()));//纳税日期
		orgTax.setTaxAmt(new BigDecimal("3200000"));//纳税金额
		orgTax.setTaxType("企业所得税");//税种
		orgTax.setRemark("企业纳税信息");//备注
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgTax);
		
		printResult(mockMvc.perform(put("/Party/Org/31/OrgTax")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取企业纳税信息
	@Test
	public void getOrgTaxTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgTax/82")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//修改企业纳税信息
	@Test
	public void updateOrgTaxTest() throws Exception {

		//企业纳税信息
		OrgTax orgTax = new OrgTax();
		orgTax.setRecordId(82);
		
		orgTax.setTaxDate(new Date(System.currentTimeMillis()));//纳税日期
		orgTax.setTaxAmt(new BigDecimal("3200000"));//纳税金额
		orgTax.setTaxType("企业所得税");//税种
		orgTax.setRemark("企业纳税信息update");//备注
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgTax);
		

		printResult(mockMvc.perform(post("/Party/Org/31/OrgTax")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//查找企业纳税信息
	@Test
	public void searchAllOrgTaxTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgTax?Action=All")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//删除企业纳税信息
	@Test
	public void delOrgTaxTest() throws Throwable {
		printResult(mockMvc.perform(delete("/Party/Org/31/OrgTax/82")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//==============================企业纳税信息结束=========================================
	
	//==============================企业欠息信息开始=========================================	
	//添加企业欠息信息
	@Test
	public void addOrgOverInterestTest() throws Exception {

		//企业欠息信息
		OrgOverInterest orgOverInterest = new OrgOverInterest();
		orgOverInterest.setOverAmt(new BigDecimal("4033333330"));//欠息金额
		orgOverInterest.setCurrency("人民币");//币种
		orgOverInterest.setOverType("人为");//欠息种类
		orgOverInterest.setOwnerName("刘xx");//被欠人姓名
		orgOverInterest.setReportDate(new Date(System.currentTimeMillis()));//报告日期
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgOverInterest);
		
		printResult(mockMvc.perform(put("/Party/Org/31/OrgOverInterest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取企业欠息信息
	@Test
	public void getOrgOverInterestTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgOverInterest/84")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//修改企业欠息信息
	@Test
	public void updateOrgOverInterTest() throws Exception {

		//企业欠息信息
		OrgOverInterest orgOverInterest = new OrgOverInterest();
		orgOverInterest.setRecordId(84);
		
		orgOverInterest.setOverAmt(new BigDecimal("4033333330"));//欠息金额
		orgOverInterest.setCurrency("人民币");//币种
		orgOverInterest.setOverType("人为");//欠息种类
		orgOverInterest.setOwnerName("刘xx update");//被欠人姓名
		orgOverInterest.setReportDate(new Date(System.currentTimeMillis()));//报告日期
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgOverInterest);
		

		printResult(mockMvc.perform(post("/Party/Org/31/OrgOverInterest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//查找企业欠息信息
	@Test
	public void searchAllOrgOverInterestTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgOverInterest?Action=All")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//删除企业欠息信息
	@Test
	public void delOrgOverInterestTest() throws Throwable {
		printResult(mockMvc.perform(delete("/Party/Org/31/OrgOverInterest/84")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//==============================企业欠息信息结束=========================================
	
	//==============================企业报表信息开始=========================================	
	//添加企业报表信息
	@Test
	public void addOrgReportTest() throws Exception {

		//企业报表信息
		OrgReport orgReport = new OrgReport();
		orgReport.setSubName("流水统计报表");//科目名称
		orgReport.setAmt(new BigDecimal("3000000"));//发生额
		orgReport.setBalance(new BigDecimal("2000000"));//余额
		orgReport.setInOrout("收入");//收入或支出
		orgReport.setReportDate(new Date(System.currentTimeMillis()));//报表日期
		orgReport.setLevel("2");//级别
		orgReport.setpSubName("100101");//上级科目编号
		orgReport.setReportType("日报表");//报表类型
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgReport);
		
		printResult(mockMvc.perform(put("/Party/Org/31/OrgReport")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取企业报表信息
	@Test
	public void getOrgReportTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgReport/85")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//修改企业报表信息
	@Test
	public void updateOrgReportTest() throws Exception {

		//企业报表信息
		OrgReport orgReport = new OrgReport();
		orgReport.setRecordId(85);
		
		orgReport.setSubName("流水统计报表update");//科目名称
		orgReport.setAmt(new BigDecimal("3000000"));//发生额
		orgReport.setBalance(new BigDecimal("2000000"));//余额
		orgReport.setInOrout("收入");//收入或支出
		orgReport.setReportDate(new Date(System.currentTimeMillis()));//报表日期
		orgReport.setLevel("2");//级别
		orgReport.setpSubName("100101");//上级科目编号
		orgReport.setReportType("日报表");//报表类型
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgReport);
		

		printResult(mockMvc.perform(post("/Party/Org/31/OrgReport")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//查找企业报表信息
	@Test
	public void searchAllOrgReportTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgReport?Action=All")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//删除企业报表信息
	@Test
	public void delOrgReportTest() throws Throwable {
		printResult(mockMvc.perform(delete("/Party/Org/31/OrgReport/85")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//==============================企业报表信息结束=========================================
	
	
	//==============================企业其他信用开始=========================================	
	//添加企业其他信用
	@Test
	public void addOrgOtherCreditTest() throws Exception {

		//企业其他信用
		OrgOtherCredit orgOtherCredit = new OrgOtherCredit();
		orgOtherCredit.setEntPraise("良好");//公司口碑
		orgOtherCredit.setEventType("被诈骗欠债");//事件类型
		orgOtherCredit.setEventDesc("被诈骗导致欠债，按时还清债务");//事件描述
		orgOtherCredit.setInfoSrc("互联网");//信息来源
		orgOtherCredit.setAddTime(new Date(System.currentTimeMillis()));//添加时间
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgOtherCredit);
		
		printResult(mockMvc.perform(put("/Party/Org/31/OrgOtherCredit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取企业其他信用
	@Test
	public void getOrgOtherCreditTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgOtherCredit/87")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//修改企业其他信用
	@Test
	public void updateOrgOtherCreditTest() throws Exception {

		//企业其他信用
		OrgOtherCredit orgOtherCredit = new OrgOtherCredit();
		orgOtherCredit.setRecordId(87);
		
		orgOtherCredit.setEntPraise("良好");//公司口碑
		orgOtherCredit.setEventType("被诈骗欠债");//事件类型
		orgOtherCredit.setEventDesc("被诈骗导致欠债，按时还清债务 update");//事件描述
		orgOtherCredit.setInfoSrc("互联网");//信息来源
		orgOtherCredit.setAddTime(new Date(System.currentTimeMillis()));//添加时间
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgOtherCredit);
		

		printResult(mockMvc.perform(post("/Party/Org/31/OrgOtherCredit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//查找企业其他信用
	@Test
	public void searchAllOrgOtherCreditTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgOtherCredit?Action=All")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//删除企业其他信用
	@Test
	public void delOrgOtherCreditTest() throws Throwable {
		printResult(mockMvc.perform(delete("/Party/Org/31/OrgOtherCredit/87")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//==============================企业其他信用结束=========================================
	
	//==============================企业不良信贷开始=========================================	
	//添加企业不良信贷
	@Test
	public void addOrgBadCreditTest() throws Exception {

		//企业不良信贷
		OrgBadCredit orgBadCredit = new OrgBadCredit();
		orgBadCredit.setCreditType("基建贷款");//业务种类
		orgBadCredit.setContractNum("HT-19123123");//合同编号
		orgBadCredit.setCurrency("人民币");//币种
		orgBadCredit.setAmt(new BigDecimal("200000000"));//发生额
		orgBadCredit.setBalance(new BigDecimal("340000000"));//当前余额
		orgBadCredit.setIsSettle("未结清");//是否结清
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgBadCredit);
		
		printResult(mockMvc.perform(put("/Party/Org/31/OrgBadCredit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取企业不良信贷
	@Test
	public void getOrgBadCreditTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgBadCredit/89")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//修改企业不良信贷
	@Test
	public void updateOrgBadCreditTest() throws Exception {

		//企业不良信贷
		OrgBadCredit orgBadCredit = new OrgBadCredit();
		orgBadCredit.setRecordId(89);
		
		orgBadCredit.setCreditType("基建贷款 update");//业务种类
		orgBadCredit.setContractNum("HT-19123123");//合同编号
		orgBadCredit.setCurrency("人民币");//币种
		orgBadCredit.setAmt(new BigDecimal("200000000"));//发生额
		orgBadCredit.setBalance(new BigDecimal("340000000"));//当前余额
		orgBadCredit.setIsSettle("未结清");//是否结清
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgBadCredit);

		printResult(mockMvc.perform(post("/Party/Org/31/OrgBadCredit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//查找企业不良信贷
	@Test
	public void searchAllOrgBadCreditTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgBadCredit?Action=All")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//删除企业不良信贷
	@Test
	public void delOrgBadCreditTest() throws Throwable {
		printResult(mockMvc.perform(delete("/Party/Org/31/OrgBadCredit/89")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//==============================企业不良信贷结束=========================================
	
	//==============================企业负债开始=========================================	
	//添加企业负债
	@Test
	public void addOrgDebtTest() throws Exception {

		//企业负债
		OrgDebt orgDebt = new OrgDebt();
		orgDebt.setBorrower("孙XX");//借款人姓名
		orgDebt.setCreditor("李XX");//债权人姓名
		orgDebt.setLoanAmt(new BigDecimal("30000000"));//借款金额
		orgDebt.setBalance(new BigDecimal("20000000"));//借款余额
		orgDebt.setBeginDate(new Date(System.currentTimeMillis()));//开始日期
		orgDebt.setEndDate(new Date(System.currentTimeMillis()));//截止日期
		orgDebt.setRepayType("12期按月还本付息");//还款方式
		orgDebt.setGuaranteeWay("房屋抵押");//担保方式
		orgDebt.setLoanType("车贷");//借款种类
		orgDebt.setRemark("企业负债");//情况说明
		orgDebt.setCreditAnalysis("信用良好");//信用情况分析
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgDebt);
		
		printResult(mockMvc.perform(put("/Party/Org/31/OrgDebt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取企业负债
	@Test
	public void getOrgDebtTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgDebt/91")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//修改企业负债
	@Test
	public void updateOrgDebtTest() throws Exception {

		//企业负债
		OrgDebt orgDebt = new OrgDebt();
		orgDebt.setRecordId(91);
		
		orgDebt.setBorrower("孙XX");//借款人姓名
		orgDebt.setCreditor("李XX");//债权人姓名
		orgDebt.setLoanAmt(new BigDecimal("30000000"));//借款金额
		orgDebt.setBalance(new BigDecimal("20000000"));//借款余额
		orgDebt.setBeginDate(new Date(System.currentTimeMillis()));//开始日期
		orgDebt.setEndDate(new Date(System.currentTimeMillis()));//截止日期
		orgDebt.setRepayType("12期按月还本付息");//还款方式
		orgDebt.setGuaranteeWay("房屋抵押");//担保方式
		orgDebt.setLoanType("车贷");//借款种类
		orgDebt.setRemark("企业负债 update");//情况说明
		orgDebt.setCreditAnalysis("信用良好");//信用情况分析
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgDebt);

		printResult(mockMvc.perform(post("/Party/Org/31/OrgDebt")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//查找企业负债
	@Test
	public void searchAllOrgDebtTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgDebt?Action=All")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//删除企业负债
	@Test
	public void delOrgDebtTest() throws Throwable {
		printResult(mockMvc.perform(delete("/Party/Org/31/OrgDebt/91")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//==============================企业负债结束=========================================
	
	
	//==============================企业资产开始=========================================	
	//添加资产负债
	@Test
	public void addOrgAssetsTest() throws Exception {

		//企业资产
		OrgAssets orgAssets = new OrgAssets();
		
		orgAssets.setAssetsType("房地产");//资产类型
		orgAssets.setBuyDate(new Date(System.currentTimeMillis()));//购买日期
		orgAssets.setBuyPrice(new BigDecimal("100000000"));//购入价格 
		orgAssets.setMonthAmt(new BigDecimal("10000000"));//月供
		orgAssets.setNowPrice(new BigDecimal("200000000"));//现在估价
		orgAssets.setOwner("张岩");//产权人
		orgAssets.setOwnerPhone("13892838383");//产权人联系电话
		orgAssets.setLoanAmt(new BigDecimal("120000000"));//贷款金额
		orgAssets.setAssetsDesc("资产负债");//资产描述
		orgAssets.setAssetsData("资产数据");//资产数据
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgAssets);
		
		printResult(mockMvc.perform(put("/Party/Org/31/OrgAssets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取企业资产
	@Test
	public void getOrgAssetsTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgAssets/93")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//修改企业资产
	@Test
	public void updateOrgAssetsTest() throws Exception {

		//企业资产
		OrgAssets orgAssets = new OrgAssets();
		orgAssets.setRecordId(93);
		
		orgAssets.setAssetsType("房地产");//资产类型
		orgAssets.setBuyDate(new Date(System.currentTimeMillis()));//购买日期
		orgAssets.setBuyPrice(new BigDecimal("100000000"));//购入价格 
		orgAssets.setMonthAmt(new BigDecimal("10000000"));//月供
		orgAssets.setNowPrice(new BigDecimal("200000000"));//现在估价
		orgAssets.setOwner("张岩");//产权人
		orgAssets.setOwnerPhone("13892838383");//产权人联系电话
		orgAssets.setLoanAmt(new BigDecimal("120000000"));//贷款金额
		orgAssets.setAssetsDesc("资产负债 update");//资产描述
		orgAssets.setAssetsData("资产数据");//资产数据
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(orgAssets);

		printResult(mockMvc.perform(post("/Party/Org/31/OrgAssets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//查找企业资产
	@Test
	public void searchAllOrgAssetsTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Org/31/OrgAssets?Action=All")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//删除企业资产
	@Test
	public void delOrgAssetsTest() throws Throwable {
		printResult(mockMvc.perform(delete("/Party/Org/31/OrgAssets/93")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//==============================企业资产结束=========================================
	
	
	
	//==============================平台用户(自然人基本信息)开始=========================================
	//添加平台用户(自然人基本信息)
	@Test
	public void addPerLegalPersonTest() throws Exception {

		//汇金贷平台用户信息
		PartyPerBean partyPerBean = new PartyPerBean();
//		partyPerBean.setAge(10);
//		partyPerBean.setState((short)0);
		partyPerBean.setParTypeId(0);//必输项
//		partyPerBean.setHomeAddr("北京大兴区枣园彩虹新城xx楼xx单元xxx");
//		partyPerBean.setNote("黄晓辉");
//		partyPerBean.setCreateDay(new Date(System.currentTimeMillis()));
//		partyPerBean.setState((short)0);
//		partyPerBean.setPostAddr("北京大兴区枣园彩虹新城xx楼xx单元xxx");
//		partyPerBean.setWeiXinNo("1111111");
//		partyPerBean.setQqNo("2222222");
//		partyPerBean.setLevel(1);
//		partyPerBean.setIdNo("371324198304176119");
//		partyPerBean.setAge(31);
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(partyPerBean);
		
		printResult(mockMvc.perform(put("/Party/Per")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//修改平台用户(自然人基本信息)
	@Test
	public void updatePerLegalPersonTest() throws Exception {

		//汇金贷平台用户信息
		PartyPerBean partyPerBean = new PartyPerBean();
		partyPerBean.setPartyId(33L);
		
		partyPerBean.setAge(10);
		partyPerBean.setState((short)0);
		partyPerBean.setParTypeId(0);
		partyPerBean.setHomeAddr("北京大兴区枣园彩虹新城xx楼xx单元xxx update2");
		partyPerBean.setNote("黄晓辉 update");
		partyPerBean.setCreateDay(new Date(System.currentTimeMillis()));
		partyPerBean.setState((short)0);
		partyPerBean.setPostAddr("北京大兴区枣园彩虹新城xx楼xx单元xxx update2");
		partyPerBean.setWeiXinNo("1111111");
		partyPerBean.setQqNo("2222222");
		partyPerBean.setLevel(1);
		partyPerBean.setIdNo("371324198304176119");
		partyPerBean.setAge(31);
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(partyPerBean);
		
		printResult(mockMvc.perform(post("/Party/Per/33")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//删除平台用户(自然人基本信息)
	@Test
	public void delPerLegalPersonTest() throws Exception {

		printResult(mockMvc.perform(delete("/Party/Per/30")
                .contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	//获取平台用户(自然人基本信息)
	@Test
	public void getPerLegalPersonTest() throws Throwable {
		printResult(mockMvc.perform(get("/Party/Per/33")
				.contentType(MediaType.APPLICATION_JSON)
				)
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString()
				);
	}
	//==============================平台用户(自然人基本信息)结束=========================================
	
	*//**
	 * 添加机构
	 *//*
	@Test
	public void addSysOrg() throws Exception {

		//机构信息
		PartyOrgBean partyOrgBean = new PartyOrgBean();
		partyOrgBean.setName("机构1-1");//机构名称
		partyOrgBean.setLicNo("XKZ-0020");//经营许可证
		partyOrgBean.setRegAddr("北京市朝阳区朝外大街人寿大厦1202");//地址
		partyOrgBean.setZip("10000");//邮编
		partyOrgBean.setEmailAddr("sdf123@163.com");//Email地址
		partyOrgBean.setPhoneNo("13647874212");//电话
		partyOrgBean.setFax("58493832");//传真
		partyOrgBean.setAccBank("中国工商银行");//结算银行名称
		partyOrgBean.setAcctNo("6222010100020202020");//银行账户
		partyOrgBean.setCityCode("0010");//所在城市 (0010-北京及其周边；0024沈阳市及其周边；0755深圳市及其周边)
		partyOrgBean.setRoleTypeId(6);//6-机构
		//网点地址（机构中不设置网点儿地址）
		partyOrgBean.setNote("备注");//备注
		partyOrgBean.setState((short)0);
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(partyOrgBean);
		
		printResult(mockMvc.perform(put("/Party/Org")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonStr)
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}
	*//**
	 * 添加机构之间的关系
	 *//*
	@Test
	public void addRelation() throws Exception {

		
		printResult(mockMvc.perform(put("/Party/Org/Relation")
                .contentType(MediaType.APPLICATION_JSON)
                .param("partyId1", "60")
                .param("partyId2", "61")
                .param("roleReType", "4")//4-上下级关系
				)
				.andExpect(status().isOk())
	            .andReturn().getResponse().getContentAsString()
				);

	}*/
	
	
}
