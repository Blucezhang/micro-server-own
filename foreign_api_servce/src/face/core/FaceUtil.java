package face.core;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FaceUtil {
	
	/**
	 * Map --> Bean 1: 利用Introspector,PropertyDescriptor实现 Map --> Bean  
	 * @param map
	 * @param obj
	 * @return Bean
	 */
    public static Object transMap2Bean(Map<String, Object> map, Object obj) {  
  
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());  
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();  
  
            for (PropertyDescriptor property : propertyDescriptors) {  
                String key = property.getName();  
  
                if (map.containsKey(key)) {  
                    Object value = map.get(key);  
                    // 得到property对应的setter方法  
                    Method setter = property.getWriteMethod();  
                    setter.invoke(obj, value);  
                }  
  
            }  
        } catch (Exception e) {  
            System.out.println("transMap2Bean Error " + e);  
        }
		return obj;  
    }  
    
    
    
    /**
	 * Map --> Bean 1: 利用Introspector,PropertyDescriptor实现 Map --> Bean  
	 * @param map
	 * @param obj
	 * @return Bean
	 */
    public static Object transformMap2Bean(Map<String, Object> map, Object obj) {  
  
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass().getSuperclass());  
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();  
            
            for (PropertyDescriptor property : propertyDescriptors) {  
                String key = property.getName();  
  
                if (map.containsKey(key)) {  
                    Object value = map.get(key);  
                    // 得到property对应的setter方法  
                    Method setter = property.getWriteMethod();  
                    setter.invoke(obj, value);  
                }  
  
            }  
            
            BeanInfo beanInfo1 = Introspector.getBeanInfo(obj.getClass());  
            PropertyDescriptor[] propertyDescriptors1 = beanInfo1.getPropertyDescriptors();  
            
            for (PropertyDescriptor property : propertyDescriptors1) {  
                String key = property.getName();  
  
                if (map.containsKey(key)) {  
                    Object value = map.get(key);  
                    // 得到property对应的setter方法  
                    Method setter = property.getWriteMethod();  
                    setter.invoke(obj, value);  
                }  
  
            }  
            
        } catch (Exception e) {  
            System.out.println("transMap2Bean Error " + e);  
        }
		return obj;  
    } 
    
    
  
    /**
     * Bean --> Map 1: 利用Introspector和PropertyDescriptor 将Bean --> Map
     * @param obj
     * @return Map
     */
    public static Map<String, Object> transBean2Map(Object obj) {  
  
        if(obj == null){  
            return null;  
        }          
        Map<String, Object> map = new HashMap<String, Object>();  
        try {  
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());  
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();  
            for (PropertyDescriptor property : propertyDescriptors) {  
                String key = property.getName();  
  
                // 过滤class属性  
                if (!key.equals("class")) {  
                    // 得到property对应的getter方法  
                    Method getter = property.getReadMethod();  
                    Object value = getter.invoke(obj);  
  
                    map.put(key, value);  
                }  
  
            }  
        } catch (Exception e) {  
            System.out.println("transBean2Map Error " + e);  
        }  
  
        return map;  
    }
    
    /**
     * Bean --> Map 1: 利用Introspector和PropertyDescriptor 将Bean --> Map
     * @param obj
     * @return Map
     */
    public static Map<String, Object> transBean2MapNotNull(Object obj) {  
    	  
        if(obj == null){  
            return null;  
        }          
        Map<String, Object> map = new HashMap<String, Object>();  
        try {  
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());  
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();  
            for (PropertyDescriptor property : propertyDescriptors) {  
                String key = property.getName();  
  
                // 过滤class属性  
                if (!key.equals("class")) {  
                    // 得到property对应的getter方法  
                    Method getter = property.getReadMethod();  
                    Object value = getter.invoke(obj);  
                    if(value!=null && value!=""){
                    	map.put(key, value);  
                    }
                }  
  
            }  
        } catch (Exception e) {  
            System.out.println("transBean2Map Error " + e);  
        }  
  
        return map;  
    }  
    
    
    
    public static boolean isNullOrEmpty(Object object) {
		if (object == null || "".equals(object.toString()))
			return true;
		return false;
	}

	/**
	 * 转字符串，去空格
	 * @param object
	 * @return
	 */
	public static String toStringAndTrim(Object object) {
		if (object == null)
			return "";
		else
			return object.toString().trim();
	}
    
}
