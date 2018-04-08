package com.own.face.core;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FaceBase implements IErrCode {

	@Autowired
	@LoadBalanced
	protected RestTemplate restTemplate;

	/**
	 * 查询
	 * 
	 * @param url
	 * @param classType
	 * @param map
	 * @return <T> T
	 */
	protected <T> T get(String url, Class<T> classType, Map<String, ?> map) {
		return restTemplate.getForObject(url, classType, map);
	}

	/**
	 * 查询
	 * 
	 * @param url
	 * @param classType
	 * @param bean
	 * @return <T> T
	 */
	protected <T> T get(String url, Class<T> classType, Object bean) {
		Map<String, ?> map = FaceUtil.transBean2Map(bean);
		return restTemplate.getForObject(url, classType, map);
	}
	
	/**
	 * 添加
	 * @param url
	 * @param request
	 * @param classType
	 * @return <T> T
	 */
	protected <T> T put(String url, Object request, Class<T> classType) {
		RestTemplateExt rte = new RestTemplateExt(restTemplate);
		ObjectMapper m = new ObjectMapper();
		String requestBody = null;
		HttpEntity response = null; 
		try {
			 requestBody = m.writeValueAsString(request);
			 MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
			 headers.add("Accept", "application/json");
		     headers.add("Content-Type", "application/json;charset=UTF-8");
		     response = new HttpEntity(requestBody, headers);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return rte.put(url, response, classType);
	}
	
	/**
	 * 修改
	 * @param url
	 * @param request
	 * @param classType
	 * @param map
	 * @return <T> T
	 */
	protected <T> T post(String url, Object request, Class<T> classType,Map<String, ?> map) {
		ObjectMapper m = new ObjectMapper();
		String requestBody = null;
		HttpEntity response = null; 
		try {
			 requestBody = m.writeValueAsString(request);
			 MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
			 headers.add("Accept", "application/json");
		     headers.add("Content-Type", "application/json;charset=UTF-8");
		     response = new HttpEntity(requestBody, headers);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return restTemplate.postForObject(url, request, classType, map);
	}
	
	/**
	 * 删除
	 * @param url
	 * @param urlVariables
	 */
	protected void delete(String url, Map<String, ?> map) {
		restTemplate.delete(url, map);
	}
	
	
}
