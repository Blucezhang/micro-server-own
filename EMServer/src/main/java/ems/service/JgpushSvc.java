package ems.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ems.domain.Jgpush;

@Service
public class JgpushSvc {

	Logger log = Logger.getLogger(SmsSvc.class);
	
	@Autowired
	private RdbSvc rdbSvc;
	
	/**
	 * 保存或修改信息
	 * @param jp
	 * @return
	 */
	public Jgpush save(Jgpush jp){
		try{
			rdbSvc.save(jp);
		}catch(Exception ee){
			ee.printStackTrace();
		}
		return jp;
	}
	
	/**
	 * 根据id查询极光信息
	 * @param id
	 * @return
	 */
	public Jgpush findJgpushById(Integer id){
		String findsql = " from Jgpush jp where jp.id=:id ";
		Map<String,String> param = new HashMap<String,String>();
		Jgpush jp = null;
		try{
			jp = (Jgpush)rdbSvc.findObject(findsql,param);
		}catch(Exception ee){
			ee.printStackTrace();
		}
		return jp;
	}
	
	/**
	 * 查询列表信息
	 * @param wheresql
	 * @return
	 */
	public List<?> findJgpushs(String wheresql){
		String findsql = " from Jgpush jp where 1=1 ";
		List<?> jlist = new ArrayList();
		try{
			jlist = rdbSvc.findList(findsql, null);
		}catch(Exception ee){
			ee.printStackTrace();
		}
		return jlist;
	}
}
