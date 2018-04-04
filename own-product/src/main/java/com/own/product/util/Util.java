package com.own.product.util;


public class Util {
	
	
	/**
	 * 判断对象是否为空
	 * @param object
	 * @return
	 */
	public static boolean isNullOrEmpty(Object object){
		if(object==null||"".equals(object.toString()))
			return true;
		return false;
	}
	
	
	
}
