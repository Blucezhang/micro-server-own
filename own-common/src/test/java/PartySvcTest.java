public class PartySvcTest extends TestBase{

	
	/*// @Rollback(false)
	 @Test
	public void addPerEduTest() throws Exception {

		EduBean edu = new EduBean();
		edu.setDegreeType(1);
		edu.setShcoolName("山东大学");
		edu.setTeacherName("东方朔");
		edu.setDegreeTime(new Timestamp(System.currentTimeMillis()));

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(edu); // 返回字符串
		String result = mockMvc
				.perform(
						put("/Party/Per/1/Edu").contentType(
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
  
	 	printResult(result);
	}

		@Test
		public void searchPerEduTest() throws Exception {

			ObjectMapper mapper = new ObjectMapper();

			String result = mockMvc
					.perform(
							get("/Party/Per/35/Edu/?Action=All").contentType(
									MediaType.APPLICATION_JSON).param("partyId",
									"35")).andExpect(status().isOk()).andReturn()
					.getResponse().getContentAsString();

		 	printResult(result);
		}

	@Test   //删除教育资料
	public void delPerEduTest() throws Exception {

		
		ObjectMapper mapper = new ObjectMapper();

		String result = mockMvc
				.perform(delete("/Party/Per/1/Edu/9").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	//修改教育资料
	@Test
	public void updatePerEduTest() throws Exception {

		EduBean edu = new EduBean();
		edu.setDegreeType(1);
		edu.setShcoolName("山东大学15");
		edu.setTeacherName("东方朔15");
		edu.setRecordId(10);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(edu); // 返回字符串
		String result = mockMvc
				.perform(
						post("/Party/Per/1/Edu").contentType( 
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
	 	printResult(result);
	}
	
	@Test
	public void getPerEduTest() throws Exception {

		ObjectMapper mapper = new ObjectMapper();

		String result = mockMvc
				.perform(get("/Party/Per/1/Edu/15").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	@Test
 	public void addLinkMan() throws Exception{
		LinkMan linkMan = new LinkMan();
		linkMan.setName("父亲");
		linkMan.setCellPhone("13811564564");
		linkMan.setTelePhone("010-65881648");
 		linkMan.setLevel(1);
		linkMan.setRelation(1);//父子关系
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(linkMan); // 返回字符串
		String result = mockMvc
				.perform(
						put("/Party/Per/1/LinkMan").contentType(
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
  
	 	printResult(result);
	}
	
	@Test
 	public void delLinkMan()throws Exception{
	 
		String result = mockMvc
				.perform(delete("/Party/Per/1/LinkMan/12").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	@Test
 	public void updateLinkMan()throws Exception{
		LinkMan linkMan = new LinkMan();
		
		linkMan.setRecordId(13);
		linkMan.setName("父 亲");
		linkMan.setCellPhone("13811564565");
		linkMan.setTelePhone("010-65881649");
 		linkMan.setLevel(1);
		linkMan.setRelation(1);//父子关系

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(linkMan); // 返回字符串
		String result = mockMvc
				.perform(
						post("/Party/Per/1/LinkMan").contentType( 
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
	 	printResult(result);
	}
	
	@Test
 	public void getLinkMan()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/1/LinkMan/13").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	
	@Test
 	public void searchLinkMan()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/1/LinkMan/").contentType(MediaType.APPLICATION_JSON).param("Action","All"))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

		printResult(result);
	}
	
	//============================诉讼测试开始==================================
	@Test
 	public void addLawsuit() throws Exception{
		Lawsuit lawsuit = new Lawsuit();
		lawsuit.setDescribe("2014年因为与供应声就货款问题产生纠纷，双发上诉至北京中院");
		lawsuit.setEndDay(new Date(System.currentTimeMillis()));
		lawsuit.setSentence("经过北京中院审理，本公司获胜");
		lawsuit.setSource("互联网");
		lawsuit.setStartDay(new Date(System.currentTimeMillis()));
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(lawsuit); // 返回字符串
		String result = mockMvc
				.perform(
						put("/Party/Per/1/Lawsuit").contentType(
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
  
	 	printResult(result);
	}
	
	@Test
 	public void delLawsuit()throws Exception{
	 
		String result = mockMvc
				.perform(delete("/Party/Per/1/Lawsuit/19").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	@Test
 	public void updateLawsuit()throws Exception{
	 
		Lawsuit lawsuit = new Lawsuit();
		lawsuit.setDescribe("2014年因为与供应声就货款问题产生纠纷，双发上诉至北京中院");
		lawsuit.setEndDay(new Date(System.currentTimeMillis()));
		lawsuit.setSentence("经过北京中院审理，本公司获胜");
		lawsuit.setSource("报纸");
		lawsuit.setStartDay(new Date(System.currentTimeMillis()));
		lawsuit.setRecordId(20);
 

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(lawsuit); // 返回字符串
		String result = mockMvc
				.perform(
						post("/Party/Per/1/Lawsuit").contentType( 
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
	 	printResult(result);
	}
	
	@Test
 	public void getLawsuit()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/1/Lawsuit/20").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	
	@Test
 	public void searchLawsuit()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/1/Lawsuit/").contentType(MediaType.APPLICATION_JSON).param("Action","All"))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

		printResult(result);
	}
 	
	//============================诉讼测试结束==================================
	

	//============================工作经历开始==================================
	@Test
 	public void addJobExp() throws Exception{
		JobExp data = new JobExp();
		data.setClerkNum(100);
		data.setCorpAddr("北京市朝阳路朝外大街1号24层2404");
		data.setCorpName("北京汇金贷资产管理有限公司");
		data.setCorpOffTele("01065881648");
		data.setCorpZip("100020");
		data.setDept("数据中心");
		data.setIndustryType(1);//互联网
		data.setJoinDay(new Date(System.currentTimeMillis()));
		data.setOffDay(new Date(System.currentTimeMillis()));
		data.setPosition(5);//总经理
	 
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(data); // 返回字符串
		String result = mockMvc
				.perform(
						put("/Party/Per/1/JobExp").contentType(
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
  
	 	printResult(result);
	}
	
	@Test
 	public void delJobExp()throws Exception{
	 
		String result = mockMvc
				.perform(delete("/Party/Per/1/JobExp/25").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	@Test
 	public void updateJobExp()throws Exception{
	 
		JobExp data = new JobExp();
  
		data.setClerkNum(100);
		data.setCorpAddr("北京市朝阳路朝外大街1号24层2404TTTTT");
		data.setCorpName("北京汇金贷资产管理有限公司TTTT");
		data.setCorpOffTele("01065881648");
		data.setCorpZip("100020");
		data.setDept("开发中心");
		data.setIndustryType(1);//互联网
		data.setPosition(5);//总经理
		data.setRecordId(31);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(data); // 返回字符串
		String result = mockMvc
				.perform(
						post("/Party/Per/1/JobExp").contentType( 
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
	 	printResult(result);
	}
	
	@Test
 	public void getJobExp()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/1/JobExp/31").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	
	@Test
 	public void searchJobExp()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/1/JobExp/").contentType(MediaType.APPLICATION_JSON).param("Action","All"))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

		printResult(result);
	}
	
	
	
	//============================工作经验结束==================================	
	
	//============================个人负债开始==================================
	@Test
 	public void addDebtExp() throws Exception{
		DebtBean data = new DebtBean();
		data.setClearDay(new Date(System.currentTimeMillis()));
		data.setDebtee("北京汇金贷资产管理有限公司");
		data.setDebtState(0);//未还清
		data.setDebtTerm(24);//24期
		data.setDebtType(0);//
		data.setRemark("这是一笔大生意啊，千万不能搞砸了");
		data.setRepayedAmt(new BigDecimal(5000));
		data.setRepayType(0);
		data.setUpdateDay(new Date(System.currentTimeMillis()));
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(data); // 返回字符串
		String result = mockMvc
				.perform(
						put("/Party/Per/1/Debt").contentType(
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
  
	 	printResult(result);
	}
	
	@Test
 	public void delDebt()throws Exception{
	 
		String result = mockMvc
				.perform(delete("/Party/Per/1/Debt/32").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	@Test
 	public void updateDebt()throws Exception{
	 
		DebtBean data = new DebtBean();
		data.setClearDay(new Date(System.currentTimeMillis()));
		data.setDebtee("北京汇金贷资产管理有限公司");
		data.setDebtState(0);//未还清
		data.setDebtTerm(24);//24期
		data.setDebtType(0);//
		data.setRemark("这是啥大生意啊，骗人的");
		data.setRepayedAmt(new BigDecimal(5000));
		data.setRepayType(0);
		data.setUpdateDay(new Date(System.currentTimeMillis()));
		data.setRecordId(37);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(data); // 返回字符串
		String result = mockMvc
				.perform(
						post("/Party/Per/1/Debt").contentType( 
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
	 	printResult(result);
	}
	
	@Test
 	public void getDebt()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/1/Debt/37").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	
	@Test
 	public void searchDebt()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/1/Debt/").contentType(MediaType.APPLICATION_JSON).param("Action","All"))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

		printResult(result);
	}
 	
	//============================工作经验结束==================================	
	
	
	
	//============================个人房产开始==================================
	@Test
 	public void addPerHouse() throws Exception{
		HouseBean data = new HouseBean();
		data.setAreas(89.2F);
		data.setAssetType(0);//房产
		data.setBuyDay(nowDate);
		data.setBuyPrice(getDecimal(10200));
		data.setCurrAmt(getDecimal(500000.00F));
		data.setDebtdAmt(getDecimal(300000F));
		data.setImageFileName("/asset/house/123456.image");
		data.setMonthPayAmt(getDecimal(5000F));
		data.setMortgage(true);
		data.setRemark("地理位置好，学区房");
		data.setRightOwner("招商银行北京分行");
		data.setRightOwnerTele("010-65881648");
		 
	 
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(data); // 返回字符串
		String result = mockMvc
				.perform(
						put("/Party/Per/1/Asset/House").contentType(
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
  
	 	printResult(result);
	}
	
	@Test
 	public void delPerHouse()throws Exception{
	 
		String result = mockMvc
				.perform(delete("/Party/Per/1/Asset/House/70").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	@Test
 	public void updatePerHouse()throws Exception{
	 
		HouseBean data = new HouseBean();
  		data.setRecordId(71);//===============================修改这里记录号==============================
		data.setAreas(89.2F);
		data.setAssetType(0);//房产
		data.setBuyDay(nowDate);
		data.setBuyPrice(getDecimal(10200));
		data.setCurrAmt(getDecimal(500000.00F));
		data.setDebtdAmt(getDecimal(300000F));
		data.setImageFileName("/asset/house/1234567.image");
		data.setMonthPayAmt(getDecimal(5000F));
		data.setMortgage(true);
		data.setRemark("地理位置好，学区房11111111111");
		data.setRightOwner("招商银行北京分行1111111");
		data.setRightOwnerTele("010-6588164811");
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(data); // 返回字符串
		String result = mockMvc
				.perform(
						post("/Party/Per/1/Asset/House").contentType( 
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
	 	printResult(result);
	}
	
	@Test
 	public void getPerHouse()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/Asset/1/House/64").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	
	@Test
 	public void searchPerHouse()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/Asset/1/House/").contentType(MediaType.APPLICATION_JSON).param("Action","All"))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

		printResult(result);
	}
	
	
	
	//============================个人房产开始END==================================		
	
	
	//============================个人汽车开始==================================
	@Test
 	public void addPerCar() throws Exception{
		CarBean data = new CarBean();
		data.setColor("Yellow");
		data.setAssetType(0);//房产
		data.setBuyDay(nowDate);
		data.setBuyPrice(getDecimal(10200));
		data.setCurrAmt(getDecimal(500000.00F));
		data.setDebtdAmt(getDecimal(300000F));
		data.setImageFileName("/asset/car/123456.image");
		data.setMonthPayAmt(getDecimal(5000F));
		data.setMortgage(true);
		data.setRemark("好车啊");
		data.setRightOwner("招商银行北京分行");
		data.setRightOwnerTele("010-65881648");
		 
	 
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(data); // 返回字符串
		String result = mockMvc
				.perform(
						put("/Party/Per/1/Asset/Car").contentType(
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
  
	 	printResult(result);
	}
	
	@Test
 	public void delPerCar()throws Exception{
	 
		String result = mockMvc
				.perform(delete("/Party/Per/1/Asset/Car/66").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	@Test
 	public void updatePerCar()throws Exception{
	 
		CarBean data = new CarBean();
  		data.setRecordId(67);//===============================修改这里记录号==============================
  		data.setColor("Yellow");
		data.setAssetType(0);//房产
		data.setBuyDay(nowDate);
		data.setBuyPrice(getDecimal(10200));
		data.setCurrAmt(getDecimal(500000.00F));
		data.setDebtdAmt(getDecimal(300000F));
		data.setImageFileName("/asset/Car/1234567.image");
		data.setMonthPayAmt(getDecimal(5000F));
		data.setMortgage(true);
		data.setRemark("好车11111111111");
		data.setRightOwner("招商银行北京分行1111111");
		data.setRightOwnerTele("010-6588164811");
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(data); // 返回字符串
		String result = mockMvc
				.perform(
						post("/Party/Per/1/Asset/Car").contentType( 
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
	 	printResult(result);
	}
	
	@Test
 	public void getPerCar()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/1/Asset/Car/67").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	
	@Test
 	public void searchPerCar()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/1/Asset/Car/").contentType(MediaType.APPLICATION_JSON).param("Action","All"))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

		printResult(result);
	}
	
	
	
	//============================个人汽车END==================================		
 	
	//============================个人银行流水开始==================================
	@Test
 	public void addPerBankTran() throws Exception{
		BankTranBean data = new BankTranBean();
		data.setBalance(this.getDecimal(50000f));
		data.setBankName("中国交通银行");
		data.setCreditAmt(this.getDecimal(500000));
		data.setCreditNum(5);
		data.setDebtAmt(this.getDecimal(300000));
		data.setDebtNum(10);
		data.setEndDay(nowDate);
		data.setStartDay(nowDate);
		data.setRemark("交通银行转账交易标识");
		data.setState(0);
  	 
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(data); // 返回字符串
		String result = mockMvc
				.perform(
						put("/Party/Per/1/BankTran").contentType(
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
  
	 	printResult(result);
	}
	
	@Test
 	public void delPerBankTran()throws Exception{
	 
		String result = mockMvc
				.perform(delete("/Party/Per/1/BankTran/68").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	@Test
 	public void updatePerBankTran()throws Exception{
	 
		BankTranBean data = new BankTranBean();
  		data.setRecordId(69);//===============================修改这里记录号==============================
		data.setBalance(this.getDecimal(50000f));
		data.setBankName("中国交通银行111");
		data.setCreditAmt(this.getDecimal(500000));
		data.setCreditNum(5);
		data.setDebtAmt(this.getDecimal(300000));
		data.setDebtNum(10);
		data.setEndDay(nowDate);
		data.setStartDay(nowDate);
		data.setRemark("交通银行转账交易标识1111");
		data.setState(0);
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(data); // 返回字符串
		String result = mockMvc
				.perform(
						post("/Party/Per/1/BankTran").contentType( 
								MediaType.APPLICATION_JSON).content(jsonStr))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();
	 	printResult(result);
	}
	
	@Test
 	public void getPerBankTran()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/1/BankTran/69").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

	 	printResult(result);
	}
	
	
	@Test
 	public void searchPerBankTran()throws Exception{
		String result = mockMvc
				.perform(get("/Party/Per/1/BankTran/").contentType(MediaType.APPLICATION_JSON).param("Action","All"))
				.andExpect(status().isOk()).andReturn()
				.getResponse().getContentAsString();

		printResult(result);
	}
	
	*/
	
	//============================个人银行流水END==================================		
 	
	
}
