

public class SystemTest extends TestBase {

	 	
	 	
/*	@Test
	//获取所有的容器处理流程
	public void getSystemProcess() throws Exception {
		 
			String result = mockMvc
					.perform(get("/System/Proc/").contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn().getResponse()
					.getContentAsString();
			 
			this.printResult(result);
		}
	@Test
	//获取所有的原子交易
	public void getAllAtomTrans() throws Exception {
		 
			String result = mockMvc
					.perform(get("/System/AtomTrans").contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isOk()).andReturn().getResponse()
					.getContentAsString();
			 
			this.printResult(result);
		}
	@Test
	//读取指定产品模板的所有流程配置（无需体现顺序）
	public void getTemplateProcess() throws Exception {

		String result = mockMvc
				.perform(get("/System/Template/42/Proc").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andReturn().getResponse()
				.getContentAsString();

		this.printResult(result);
	}
	@Test
	//添加流程节点配置
//	public void addSysProcess() throws Exception {
//		
////		addSysProcess1();//添加流程节点-数据校验原子交易测试
//		addSysProcess2();//添加流程节点-模拟原子交易测试
//		
//	}
	
	private void addSysProcess1() throws Exception {
		FunSetBean funSetBean = new FunSetBean();
		funSetBean.setName("loanAdd3");
		funSetBean.setNote("发标,数据检查test");
		
		List<ProcessConfig> config = new ArrayList<ProcessConfig>();
		ProcessConfig processConfig1 = new ProcessConfig();
		processConfig1.setProcName("platformFee");
		processConfig1.setExInEl("exInEltest");
		processConfig1.setExOutEl("exOutEltest");
		String verifyConfigJson1 = new StringBuffer()
		.append("{")
		.append("    \"name\":\"amount\",")
		.append("    \"type\":\"BigDecimal\",")
		.append("    \"rules\":")
		.append("    [")
		.append("    	{")
		.append("           \"minValue\":\"10000\",")
		.append("           \"note\":\"投资金额最小为10000\"")
		.append("    	},")
		.append("    	{")
		.append("           \"maxValue\":\"1000000\",")
		.append("           \"note\":\"您最多只能投1000000\"")
		.append("    	}")
		.append("    ]")
		.append("}")
		.toString();
		processConfig1.setConfigData(verifyConfigJson1);
		config.add(processConfig1);
		
		ProcessConfig processConfig2 = new ProcessConfig();
		processConfig2.setProcName("transactionFee");
		processConfig2.setExInEl("exInEltest1");
		processConfig2.setExOutEl("exOutEltest1");
		String verifyConfigJson2 = new StringBuffer()
		.append("{")
		.append("    \"name\":\"amount2\",")
		.append("    \"type\":\"BigDecimal2\",")
		.append("    \"rules\":")
		.append("    [")
		.append("    	{")
		.append("           \"minValue\":\"10000\",")
		.append("           \"note\":\"投资金额最小为10000\"")
		.append("    	},")
		.append("    	{")
		.append("           \"maxValue\":\"1000000\",")
		.append("           \"note\":\"您最多只能投1000000\"")
		.append("    	}")
		.append("    ]")
		.append("}")
		.toString();
		processConfig2.setConfigData(verifyConfigJson2);
		config.add(processConfig2);

		ProcessConfig processConfig3 = new ProcessConfig();
		processConfig3.setProcName("changeProdState");
		processConfig3.setExInEl("exInEltest1");
		processConfig3.setExOutEl("exOutEltest1");
		String verifyConfigJson3 = new StringBuffer()
		.append("{")
		.append("  \"currentState\":\"1\",")
		.append("  \"nextState\":\"3\"")
		.append("}")
		.toString();
		processConfig3.setConfigData(verifyConfigJson3);
		config.add(processConfig3);
		
		ProcessConfig processConfig4 = new ProcessConfig();
		processConfig4.setProcName("dataCheck");
		processConfig4.setExInEl("exInEltest1");
		processConfig4.setExOutEl("exOutEltest1");
		*//**
//		String verifyConfigJson4 = new StringBuffer()
//		.append("{")
//		.append("    \"checkRules\":")
//		.append("    [")
//		.append("    	{")
//		.append("           \"defaultValue\":\"1000\",")
//		.append("           \"type\":\"Decimal\",")
//		.append("           \"name\":\"amount\",")
//		.append("           \"parseFmt\":\"#,##0.00\",")
//		.append("           \"pattern\":\"(\\d*|\\d{1,3}(,\\d{3})*)(\\.\\d{1,2})?\",")
//		.append("           \"minValue\":\"10\",")
//		.append("           \"maxValue\":\"10000000\",")
//		.append("           \"require\":\"true\",")
//		.append("           \"desc\":\"投资金额\"")
//		.append("    	}")
//		.append("    ]")
//		.append("}")
//		.toString();
//		processConfig4.setConfigData(verifyConfigJson4);
		 * 
		 *//*
//		Map map = new HashMap();
//		List<WordDef> wdList= new ArrayList<WordDef>();
//		WordDef wd = new WordDef();
//		wd.setName("amount");
//		wd.setDefaultValue("1000");
//		wd.setType("Decimal");
//		wd.setParseFmt("#,##0.00");
//		wd.setPattern("(\\d*|\\d{1,3}(,\\d{3})*)(\\.\\d{1,2})?");
//		wd.setMinValue("10");
//		wd.setMaxValue("10000000");
//		wd.setRequire(true);
//		wd.setDesc("投资金额");
//		wdList.add(wd);
//		map.put("checkRules", wdList);
//		ObjectMapper mapper = new ObjectMapper();
//		processConfig4.setConfigData(mapper.writeValueAsString(map));
//		config.add(processConfig4);
//		
//		funSetBean.setConfig(config);
//		
//		String jsonStr = mapper.writeValueAsString(funSetBean); // 返回字符串
//		String result = mockMvc
//				.perform(put("/System/Proc").contentType(
//						MediaType.APPLICATION_JSON)
//						.content(jsonStr))
//		.andReturn().getResponse().getContentAsString();
//		this.printResult(result);
	}
//	private void addSysProcess2() throws Exception {
//		FunSetBean funSetBean = new FunSetBean();
//		funSetBean.setName("simulationTransTest");
//		funSetBean.setNote("模拟交易，原子交易测试");
//		
//		List<ProcessConfig> config = new ArrayList<ProcessConfig>();
//		
//		ProcessConfig processConfig4 = new ProcessConfig();
//		processConfig4.setProcName("dataCheck");
//		processConfig4.setExInEl(null);
//		processConfig4.setExOutEl(null);
//		
//		Map map = new HashMap();
//		List<WordDef> wdList= new ArrayList<WordDef>();
//		WordDef wd = new WordDef();
//		wd.setName("amount");
//		wd.setDefaultValue("1000");
//		wd.setType("Decimal");
//		wd.setParseFmt("#,##0.00");
//		wd.setPattern("(\\d*|\\d{1,3}(,\\d{3})*)(\\.\\d{1,2})?");
//		wd.setMinValue("10");
//		wd.setMaxValue("10000000");
//		wd.setRequire(true);
//		wd.setDesc("投资金额");
//		wdList.add(wd);
//		map.put("checkRules", wdList);
//		ObjectMapper mapper = new ObjectMapper();
//		processConfig4.setConfigData(mapper.writeValueAsString(map));
//		config.add(processConfig4);
//
//		ProcessConfig processConfig1 = new ProcessConfig();
//		processConfig1.setProcName("simulationTrans");
//		processConfig1.setExInEl(null);
//		processConfig1.setExOutEl(null);
//		String verifyConfigJson1 = new StringBuffer()
//		.append("{")
//		.append("    \"name\":\"amount\",")
//		.append("    \"type\":\"BigDecimal\",")
//		.append("    \"rules\":")
//		.append("    [")
//		.append("    	{")
//		.append("           \"minValue\":\"10000\",")
//		.append("           \"note\":\"投资金额最小为10000\"")
//		.append("    	},")
//		.append("    	{")
//		.append("           \"maxValue\":\"1000000\",")
//		.append("           \"note\":\"您最多只能投1000000\"")
//		.append("    	}")
//		.append("    ]")
//		.append("}")
//		.toString();
//		processConfig1.setConfigData(verifyConfigJson1);
//		config.add(processConfig1);
//		
//		
//		funSetBean.setConfig(config);
//		
//		String jsonStr = mapper.writeValueAsString(funSetBean); // 返回字符串
//		String result = mockMvc
//				.perform(put("/System/Proc").contentType(
//						MediaType.APPLICATION_JSON)
//						.content(jsonStr))
//		.andReturn().getResponse().getContentAsString();
//		this.printResult(result);
//	}
//	
	
//	@Test
//	//修改流程节点配置
//	public void updateSysProcess() throws Exception {
//		FunSetBean funSetBean = new FunSetBean();
//		funSetBean.setName("fabiao");
//		funSetBean.setNote("fabiaoNoteUpdate");
//		funSetBean.setFunctionId(10);
//		
//		List<ProcessConfig> config = new ArrayList<ProcessConfig>();
//		ProcessConfig processConfig1 = new ProcessConfig();
//		processConfig1.setExInEl("exInEltest");
//		processConfig1.setExOutEl("exOutEltest");
//		processConfig1.setConfigData("sfsdf");
//		config.add(processConfig1);
//		ProcessConfig processConfig2 = new ProcessConfig();
//		processConfig2.setExInEl("exInEltest1");
//		processConfig2.setExOutEl("exOutEltest1");
//		processConfig2.setConfigData("sfsdf1");
//		config.add(processConfig2);
//		funSetBean.setConfig(config);
//		
//		ObjectMapper mapper = new ObjectMapper();
//		String jsonStr = mapper.writeValueAsString(funSetBean); // 返回字符串
//		String result = mockMvc
//				.perform(post("/System/Proc/10").contentType(
//						MediaType.APPLICATION_JSON)
//						.content(jsonStr))
//		.andReturn().getResponse().getContentAsString();
//		this.printResult(result);
//	}
*/}
