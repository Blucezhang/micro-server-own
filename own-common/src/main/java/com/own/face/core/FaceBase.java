package com.own.face.core;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
		HttpEntity<?> response = jsonEntity(request);
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
		HttpEntity<?> response = jsonEntity(request);
		return restTemplate.postForObject(url, response, classType, map);
	}

	private HttpEntity<?> jsonEntity(Object request) {
		Object body = request;
		if (!(request instanceof String)) {
			try {
				body = new ObjectMapper().writeValueAsString(request);
			} catch (JsonProcessingException exception) {
				throw new IllegalArgumentException("Request cannot be serialized as JSON", exception);
			}
		}
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
		return new HttpEntity<Object>(body, headers);
	}
	
	/**
	 * 删除
	 * @param url
	 * @param map
	 */
	protected void delete(String url, Map<String, ?> map) {
		restTemplate.delete(url, map);
	}
	
	
}
