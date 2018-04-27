package com.own.user.party.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jTemplate;


public class DaoBase{

 	/*@Autowired
	protected Session session = null;
*/
	/*public Neo4jTemplate getTemplate() {
		return new Neo4jTemplate(session);
	}*/
	
/*	public void addNode(Map nodeData){

		getTemplate().save(nodeData);
	}*/
	
	/*public List<Map> getList(String cypher,Map parameters){
	   Result result = getTemplate().query(cypher, parameters);
 	   ArrayList<Map> retList = new ArrayList<Map>();
	   for(Map data:result.queryResults()){
		   retList.add(data);
	   }
	   
	   return retList;
	}*/
    
}