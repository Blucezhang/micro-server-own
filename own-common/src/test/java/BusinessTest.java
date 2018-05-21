import groovy.util.XmlSlurper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
public class BusinessTest extends TestBase {

	/**
	 * getProcessInfo,业务数据子表信息查询说明：根据processId，获取业务详情
	 */
	@Test
	public void getProcessInfo() throws Throwable {

		Map<String,Object> map = new HashMap<String,Object>();

		map.put("actionName", "getProcessInfo");
		map.put("loginId", "ceshi111");
		map.put("token", "567891011224");

		Map<String,Object> transData = new HashMap<String,Object>();

		transData.put("processId", 3601);
		transData.put("dataType", 2);

		map.put("transData", transData);

		ObjectMapper mapper = new ObjectMapper();

		String jsonStr = mapper.writeValueAsString(map);

		System.err.println("jsonStr:" + jsonStr);

		putTest("/Product",jsonStr);
		}
	
	@Test //测试报文
	public void testMessage() throws Throwable {
		XmlSlurper xmlper = new XmlSlurper(); 
		Map<String,Object> map = new HashMap<String,Object>();

		map.put("actionName", "messageTest");
		Map<String,Object> transData = new HashMap<String,Object>();

		transData.put("processId", 3601);
		transData.put("dataType", 2);

		map.put("transData", transData);

   
		ObjectMapper mapper = new ObjectMapper();

		String jsonStr = mapper.writeValueAsString(map);

		System.err.println("jsonStr:" + jsonStr);

		putTest("/Product",jsonStr);
		}
	

	/**
	 * searchApplyInfo   申请信息列表查询
	 */
	@Test
	public void searchApplyInfoTest() throws Throwable{

		Map<String,Object> map = new HashMap<String,Object>();

		map.put("actionName", "searchApplyInfo");

		Map<String,Object> transData = new HashMap<String,Object>();

		Map<String,Object> search = new HashMap<String,Object>();

		search.put("bizType", 2);
		search.put("dataState", 1);
		search.put("pageSize", 999);
		search.put("pageNo", 0);

		transData.put("search", search);

		map.put("transData", transData);

		ObjectMapper mapper = new ObjectMapper();

		String jsonStr = mapper.writeValueAsString(map);

		System.err.println("jsonStr:" + jsonStr);

		putTest("/Product",jsonStr);
		}

	/**
	 * addProcess
	 * 申请流程 - 添加
	 */
	@Test
	public void addProcessTest() throws Throwable{

		Map<String,Object> map = new HashMap<String,Object>();

		map.put("actionName", "addProcess");

		Map<String,Object> transData = new HashMap<String,Object>();

		Map<String,Object> precessEntity = new HashMap<String,Object>();

		precessEntity.put("bizType", 2);
		precessEntity.put("bizName", "保险业务");
		precessEntity.put("originOrg", 99L);
		precessEntity.put("operator", 9911L);
		precessEntity.put("district", "北京市");

		transData.put("precessEntity", precessEntity);

		map.put("transData", transData);

		ObjectMapper mapper = new ObjectMapper();

		String jsonStr = mapper.writeValueAsString(map);

		System.err.println("jsonStr:" + jsonStr);

		putTest("/Product",jsonStr);
		}

	/**
	 * saveApplyInfo
	 * 申请信息 - 添加/补充/删除/审批
	 */
	@Test
	public void saveApplyInfoTest() throws Throwable{

		Map<String,Object> map = new HashMap<String,Object>();

		map.put("actionName", "saveApplyInfo");

		Map<String,Object> transData = new HashMap<String,Object>();

		transData.put("processId", 614L);
		transData.put("dataType", 2);	// 2-业务申请
		transData.put("originOrg", 99L);
		transData.put("district", "北京市");
		transData.put("managerId", 991L);
		transData.put("lastOper", 9911L);
		transData.put("dataState", 1);	//	状态待定

		transData.put("name", "申请人1");
		transData.put("appId", 10000L);
		transData.put("bizType", 2);	//	2-保险业务
		transData.put("country", "中国");
		transData.put("applyDistrict", "北京市");

		Map<String,Object> applyData = new HashMap<String,Object>();

		applyData.put("prodType", 1);	//	类型
		applyData.put("code", "010101");	//	保险编号
		applyData.put("note", "XX公司保险理财");	//	保险名称
		applyData.put("amount", 0);	//	保险总金额，默认为0
		applyData.put("minAmt", new BigDecimal(1000.0));	//	起投保费
		applyData.put("term", 3);	//	建议持有期限
		applyData.put("rate", 0.06);	//	历史年化收益率
		applyData.put("acctId", 100001L);	//	申请人账户标识

		transData.put("applyData", applyData);

		map.put("transData", transData);

		ObjectMapper mapper = new ObjectMapper();

		String jsonStr = mapper.writeValueAsString(map);

		System.err.println("jsonStr:" + jsonStr);

		putTest("/Product",jsonStr);
		}

	/**
	 * saveExtInfo
	 * 扩展信息 - 添加/删除
	 */
	@Test
	public void saveExtInfoTest() throws Throwable{

		Map<String,Object> map = new HashMap<String,Object>();

		map.put("actionName", "saveExtInfo");

		Map<String,Object> transData = new HashMap<String,Object>();

		transData.put("processId", 16L);
		transData.put("dataType", 2);	// 2-业务申请
		transData.put("originOrg", 99L);
		transData.put("district", "北京市");
		transData.put("managerId", 991L);
		transData.put("lastOper", 9911L);
		transData.put("dataState", 1);	//	状态待定

		transData.put("note", "业务说明");
		transData.put("catalog", 5);	//	5-业务说明

		List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();

		Map<String,Object> params = new HashMap<String,Object>();

		params.put("name", "收益说明");	//	名称
		params.put("content", "收益说明收益说明收益说明");	//	内容

		data.add(params);

		params = new HashMap<String,Object>();

		params.put("name", "还款说明");	//	名称
		params.put("content", "还款说明还款说明还款说明");	//	内容

		data.add(params);

		transData.put("data", data);

		map.put("transData", transData);

		ObjectMapper mapper = new ObjectMapper();

		String jsonStr = mapper.writeValueAsString(map);

		System.err.println("jsonStr:" + jsonStr);

		putTest("/Product",jsonStr);
		}

	/**
	 * issueProdInfo
	 * 产品信息 - 发布
	 */
	@Test
	public void issueProdInfoTest() throws Throwable {

		Map<String,Object> map = new HashMap<String,Object>();

		map.put("actionName", "issueProdInfo");

		Map<String,Object> transData = new HashMap<String,Object>();

		transData.put("processId", 1636L);
		transData.put("templateId", 153);	// 2-业务申请

		map.put("transData", transData);

		ObjectMapper mapper = new ObjectMapper();

		String jsonStr = mapper.writeValueAsString(map);

		System.err.println("jsonStr:" + jsonStr);

		putTest("/Product",jsonStr);
		}

	/**
	 * searchBoardInfo   挂盘信息列表查询
	 */
	@Test
	public void searchBoardInfoTest() throws Throwable{

		/*ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("searchBoardInfo");
		Map transData = new HashMap();
		
		Map processMap = new HashMap();
		processMap.put("dataState", 1);
		processMap.put("productType", 1);
		processMap.put("pageSize", 1);
		processMap.put("pageNo", 0);
		
		transData.put("search", processMap) ;
		invest.setTransData(transData);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	
	*//***
	 * searchProdPlanInfo 计划列表查询
	 *//*
	@Test
	public void searchProdPlanInfoTest() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("searchProdPlanInfo");
		Map transData = new HashMap();
		
		Map processMap = new HashMap();
		processMap.put("prodId", 3);
		
		transData.put("search", processMap) ;
		invest.setTransData(transData);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	
	*//**
	 * searchInvestment  投资信息列表查询
	 *//*
	@Test
	public void searchInvestmentInfoTest() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("searchInvestment");
		Map transData = new HashMap();
		
		Map processMap = new HashMap();
		processMap.put("pageSize", 1);
		processMap.put("pageNo", 0);
		processMap.put("bizType", 7);
		
		transData.put("search", processMap) ;
		invest.setTransData(transData);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	
	*//**
	 * searchArchiveTypeInfo  获取档案类型列表查询
	 *//*
	@Test
	public void searchArchiveTypeInfoTest() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("searchArchiveTypeInfo");
		Map transData = new HashMap();
		
		Map processMap = new HashMap();
		processMap.put("bizType", 7);
		
		transData.put("search", processMap) ;
		invest.setTransData(transData);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	
	
	*//**
	 * saveArchiveInfo  档案信息 - 添加/更新/删除
	 *//*
	@Test
	public void saveArchiveInfoTest() throws Throwable{
		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("saveArchiveInfo");
		Map transData = new HashMap();
		
		FinFileInfo ffInfo = new FinFileInfo();
		ffInfo.setProcessId(17L);
		ffInfo.setFileDesc("添加文件");
		transData.put("baseInfo", ffInfo) ;
		
		Map fileMsg1 = new HashMap();
		fileMsg1.put("fileName", "身份证");
		fileMsg1.put("defId", "1");
		
		SysInfoFile sysFile = new SysInfoFile();
		sysFile.setFileName("1440054099365.txt");
		sysFile.setClientFileName("测试文件名.txt");
		
		List fileList = new LinkedList<>();
		
		fileList.add(sysFile);
		fileMsg1.put("fileList", fileList);
		
		List fileMsg = new LinkedList<>();
		fileMsg.add(fileMsg1);
		
		transData.put("fileMsg", fileMsg) ;
		
		invest.setTransData(transData);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	
	
	*//**
	 * saveContractInfo  合同信息 - 添加/更新/删除
	 *//*
	@Test
	public void saveContractInfoTest() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("saveContractInfo");
		Map transData = new HashMap();
		
		Map processMap = new HashMap();
		processMap.put("dataId", 4092L);
		processMap.put("fileName", "1440054099365.txt");
		processMap.put("clientFileName", "合同添加.txt");
		processMap.put("lastOper", 1);
		processMap.put("processId", 17L);
		processMap.put("dataType", 6);
		processMap.put("originOrg", 1);
		processMap.put("district", "shanxi");
		processMap.put("managerId", 1);
		processMap.put("amount", 2);
		processMap.put("conTractType", 1);
		processMap.put("conType", 1);
		
		List<FinContractRole> roleList = new LinkedList<FinContractRole>();
		FinContractRole role = new FinContractRole();
		Party p = new Party(55L);
		System.out.println("background party id : "+p.getPartyId());
		role.setParty(p);
		role.setRoleType(1);
		roleList.add(role);
		
		processMap.put("roleList", roleList);
		
		transData.put("contractEntity", processMap) ;
		invest.setTransData(transData);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	
	*//**
	 * saveFileInfo  保存文件
	 *//*
	@Test
	public void saveFileInfoTest() throws Throwable{

		ProductActionBean invest = new ProductActionBean();
		invest.setLoginId("1");
		//invest.setLoginName("test2");
		invest.setToken("134534343");
		invest.setActionName("saveFileInfo");
		Map transData = new HashMap();
		
		Map processMap = new HashMap();
		processMap.put("dataId", 4097L);
		processMap.put("fileName", "1440054099365.txt");
		processMap.put("clientFileName", "文件保存.txt");
		processMap.put("docType", 1);
		processMap.put("upPartyId", 55L);
				
		transData.put("fileEntity", processMap) ;
		invest.setTransData(transData);

		ObjectMapper mapper = new ObjectMapper();
		String jsonStr = mapper.writeValueAsString(invest); 

		putTest("/Product", jsonStr);
	}
	
	*//**
	 * searchArchiveInfoList   申请信息列表查询
	 *//*
	@Test
	public void searchArchiveInfoListTest() throws Throwable{

		Map<String,Object> map = new HashMap<String,Object>();

		map.put("actionName", "searchArchiveInfoList");

		Map<String,Object> transData = new HashMap<String,Object>();

		Map<String,Object> search = new HashMap<String,Object>();

		search.put("processId", 17L);

		transData.put("search", search);

		map.put("transData", transData);

		ObjectMapper mapper = new ObjectMapper();

		String jsonStr = mapper.writeValueAsString(map);

		System.err.println("jsonStr:" + jsonStr);

		putTest("/Product",jsonStr);
		}
	
	
	*//**
	 * searchContractInfo   申请信息列表查询
	 *//*
	@Test
	public void searchContractInfoTest() throws Throwable{

		Map<String,Object> map = new HashMap<String,Object>();

		map.put("actionName", "searchContractInfo");

		Map<String,Object> transData = new HashMap<String,Object>();

		Map<String,Object> search = new HashMap<String,Object>();

		search.put("processId", 17L);

		transData.put("search", search);

		map.put("transData", transData);

		ObjectMapper mapper = new ObjectMapper();

		String jsonStr = mapper.writeValueAsString(map);

		System.err.println("jsonStr:" + jsonStr);

		putTest("/Product",jsonStr);
		}
	
	
	*//**
	 * downloadFileByName   申请信息列表查询
	 *//*
	@Test
	public void downloadFileByNameTest() throws Throwable{

		Map<String,Object> map = new HashMap<String,Object>();

		map.put("actionName", "downloadFileByName");

		Map<String,Object> transData = new HashMap<String,Object>();

		transData.put("fileName", "1440054099365.txt");

		map.put("transData", transData);

		ObjectMapper mapper = new ObjectMapper();

		String jsonStr = mapper.writeValueAsString(map);

		System.err.println("jsonStr:" + jsonStr);

		putTest("/Product",jsonStr);
		}*/

	}}
