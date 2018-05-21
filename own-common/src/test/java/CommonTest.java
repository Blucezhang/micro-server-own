


public class CommonTest extends TestBase {

	/**
	 * 通用多实体添加
	 */
	/*@Test
	public void insertMultiData() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("TestMultiInsert");
		
		Map map = new HashMap();
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	*//**
	 * 通用添加
	 *//*
	@Test
	public void insertData() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("insertData");
		
		Map map = new HashMap();
		map.put("entryClassType", "domain.party.PartyPerson");
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	*//**
	 * 通用修改
	 *//*
	@Test
	public void updateData() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("updateData");
		
		Map map = new HashMap();
		map.put("entryClassType", "domain.party.PartyPerson");
		map.put("partyId", 200);
		map.put("parTypeId", 0);
		map.put("postAddr", "地址测试");
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	*//**
	 * 通用删除
	 *//*
	@Test
	public void deleteData() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("deleteData");
		
		Map map = new HashMap();
		map.put("entryClassType", "domain.party.PartyPerson");
		map.put("partyId", 200);
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	*//**
	 * 通用查询 
	 *//*
	@Test
	public void searchData() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("Testtools");
		
		Map<String,Object> search = new HashMap<String,Object>();
		search.put("paramsMap", new HashMap(){{
			put("attrType", 3);
			put("prodId", 18);//这里的id如果是Long要配置数据校验来转换类型，否则查询的时候会报数据类型错误
		}});
		Map map = new HashMap();
		map.put("pageNo", 0);
		map.put("pageSize", 10);
		map.put("search", search);
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	*//**
	 * 还款测试
	 *//*
	@Test
	public void repayTest() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("testRepay");
		
		Map<String,Object> search = new HashMap<String,Object>();
		search.put("paramsMap", new HashMap(){{
			put("accProdId", 55);
		}});
		Map map = new HashMap();
		map.put("pageNo", 0);
		map.put("pageSize", 10);
		map.put("search", search);
		invest.setTransData(map);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}*/
	
}
