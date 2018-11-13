package com.own.owncommon.util;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@Slf4j
public class IocUtil implements ApplicationContextAware {
	
	public IocUtil(){
		log.info("IocUtil Start...");
	}

	private static ApplicationContext singleContainer = null;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		singleContainer=applicationContext;
	}
	
	public static Object getBean(String beanName){
		if(singleContainer==null){
			return null;
		}else{
			return singleContainer.getBean(beanName);
		}
	}
	
	



}
