package ems.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import ems.domain.CommonInfo;

@Service
public class CommonInfoSvc {
	
	@Autowired
	private RdbSvc rdbSvc;
	
	Logger logger = Logger.getLogger(CommonInfo.class);
	
	/**
	 * 添加或修改公共信息表
	 * @param cif
	 * @return
	 */
	public CommonInfo save(CommonInfo cif){
		
		if(cif!=null){
			try{
				rdbSvc.save(cif);
			}catch(Exception e){
				logger.error("保存公共信息表失败，原因："+e.getMessage());
				e.printStackTrace();
			}
		}
		return cif;
	}
	
	/**
	 * 查询公共信息
	 * @return
	 */
	public List<?> findList(String wheresql){
		Map<String,Object> param = new HashMap<String,Object>();
		String findInfoSql=" from CommonInfo c where 1=1 ";
		Page<?> result =null;
		try{
			result = (Page<?>)rdbSvc.findAll2Page(findInfoSql+wheresql, param);
			
		}catch(Exception e){
			logger.info("查询出现异常，原因："+e.getMessage());;
		}
		return  result.getContent();
	}
}
