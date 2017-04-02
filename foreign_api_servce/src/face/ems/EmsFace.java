package face.ems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import face.core.FaceBase;
import face.core.IfException;

@Service
public class EmsFace extends FaceBase{
	
	@Autowired        // Created automatically by Spring Cloud
    @LoadBalanced
    protected RestTemplate restTemplate = null; 
	protected String serviceUrl="http://EMSERVER/";
	 
	Logger log = Logger.getLogger(EmsFace.class);
	
	/**
	 * 获取邮件信息
	 * @param id
	 * @return
	 * @throws IfException
	 */
	public Email getEmailById(int id) throws IfException{
		
		Email email = restTemplate.getForObject(serviceUrl + "/Info/email/{id}", Email.class, id);
		
		return email;
	} 
	
	public Map getString(){
		System.out.println("进入此方法.....");
		String ss = "234343";
		Map aa = restTemplate.getForObject(serviceUrl + "/Test", Map.class, ss);
		return aa;
	}
	
	
	/**
	 * 发送短信
	 * @param id
	 * @param content
	 * @return
	 * @throws IfException
	 */
	public String sendMsg(String mobile,String content) throws IfException{
		
		Sms sms = new Sms();
		sms.setMobile(mobile);
		sms.setContent(content);
		//发送短信使用post,即create
		
		String url = serviceUrl+"/Info/sms";
//		Object request = "";
		Map<String,String> params = new HashMap<String,String>();
		params.put("Action", "send");
		params.put("mobile", mobile);
		params.put("content", content);
		try{
			/**
			 * 调用接口
			 */
			MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
	        headers.add("Accept", "application/json");
	        headers.add("Content-Type", "application/json;charset=utf-8");
	        String requestBody = "{\"Action\":\"send\",\"mobile\":\""+mobile+"\",\"content\":\""+content+"\"}";
	        HttpEntity request = new HttpEntity(requestBody, headers);
			
	        String responseText = restTemplate.postForObject(url, request,String.class, "");
	        //待测试request响应内容
	        
	        log.info("获取响应内容："+request.getBody());
//	        request.getBody();
			return responseText;
			
		}catch(Exception ee){
			ee.printStackTrace();
		}
		return "failed";
		
	} 
	
	
	/**
	 * 接收短信
	 * @param id
	 * @param content
	 * @return
	 * @throws IfException
	 */
	public String receiveMsg(String mobile,String content) throws IfException{
		
		Sms sms = new Sms();
		sms.setMobile(mobile);
		sms.setContent(content);
		//发送短信使用post,即create
		
		String url = serviceUrl+"/Info/sms";
//		Object request = "";
		Map<String,String> params = new HashMap<String,String>();
		params.put("Action", "receive");
		params.put("mobile", mobile);
		params.put("content", content);
		try{
			/**
			 * 经过测试，header和requestbody必须加上，否则报一大堆错误，诸如：IO错误， I/O error on POST request或者content-leng等错误
			 */
			MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
	        headers.add("Accept", "application/json");
	        headers.add("Content-Type", "application/json");
	        String requestBody = "{\"Action\":\"receive\",\"mobile\":\""+mobile+"\",\"content\":\""+content+"\"}";//传入服务端类型为map，后续可以改为对象提交
	        HttpEntity request = new HttpEntity(requestBody, headers);
			
	        String responseText = restTemplate.postForObject(url, request,String.class, "");
//			restTemplate.postForObject(serviceUrl + "/info/sms", request, Sms.class,"");//此种方式与上面调用结果相同
	        //待测试request响应内容
	        
	        log.info("获取响应内容："+request.getBody());
//	        request.getBody();
			return responseText;
			
		}catch(Exception ee){
			ee.printStackTrace();
		}
		return "failed";
		
	} 
	
	
	/**
	 * 查询短信列表
	 * @return 短信集合
	 */
	public List findSMSList(){
		Map map = new HashMap();
		map.put("id", 1);
		map.put("title", "我是标题");
		map.put("content", "我是内容");
		/**
		 * 服务端资源地址为：/info/sms，服务端获取的参数，在方法参数中使用@RequestParam定义，不在url中定义，若资源中包含参数的，在方法参数中使用@PathVariable定义
		 * 
		 */
		List list = restTemplate.getForObject(serviceUrl + "/Info/sms?id={id}&title={title}&content={content}", ArrayList.class,map);//一：此种方法可以亦可以传值,按照map变量值匹配
		
		return list;
	}
	
	/**
	 * 根据id查询短信信息
	 * @param id
	 * @return
	 */
	public Sms findSmsById(Integer id){
		Sms sms = null;
		try{
		    sms = restTemplate.getForObject(serviceUrl + "/Info/sms/{id}", Sms.class,id);
			if(sms!=null){
				log.info("短信id："+sms.getId()+" 短信内容："+sms.getContent()+" 短信创建时间:"+sms.getCreateTime());
			}
			
		}catch(Exception ee){
			ee.printStackTrace();
		}
		return sms;
	}
	
	
	
	
	
	
	/**
	 * 修改短信内容
	 * @return
	 */
	public Sms updateSms(Sms sms){
		log.info("修改短信内容....");
		
//		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
		
        HttpEntity<Sms> request = new HttpEntity<Sms>(sms);//传入服务端类型为实体
        
        String result = "";
        //以下两种调用方式，效果相同
//		restTemplate.put(serviceUrl + "/info/sms", request, result);
		restTemplate.exchange(serviceUrl + "/Info/sms", HttpMethod.PUT, request, Sms.class);

		
		log.info("值修改后："+sms.getDraft());
		log.info("result:"+result);
		return sms;
	}
	
	
	
	/**
	 * 查询公共信息列表
	 * @return 信息集合
	 */
	public List findCommonInfoList(){
		Map<String,String> map = new HashMap<String,String>();
		map.put("id", "1");
		map.put("title", "我是标题");
		map.put("content", "a");
		map.put("sendAccount", "132");
		map.put("receiveAccount", "1112");
		System.out.println("参数："+map.toString());
		String param="/Info?id={id}&title={title}";
		param +="&content={content}&sendAccount={sendAccount}&receiveAccount={receiveAccount}";
		List list = restTemplate.getForObject(serviceUrl+param, List.class,map);
		return list;
	}
	
	/**
	 * 根据id查询邮件信息
	 * @param id
	 * @return
	 */
	public Email findEmailById(Integer id){
		Email email = null;
		try{
		    email = restTemplate.getForObject(serviceUrl + "/Info/email/{id}", Email.class,id);
			if(email!=null){
				log.info("短信id："+email.getId()+" 短信内容："+email.getContent()+" 短信创建时间:"+email.getCreateTime());
			}
			
		}catch(Exception ee){
			ee.printStackTrace();
		}
		return email;
	}
	
	
	/**
	 * 发送邮件
	 * @param action、title、receiveemail
	 * @param content
	 * @return
	 * @throws IfException
	 */
	public String sendEmail(String receiveAccount,String title,String content) throws IfException{
		
		Email email = new Email();
		email.setReceiveEmail(receiveAccount);
		email.setTitle(title);
		email.setContent(content);
		
		String url = serviceUrl+"/Info/email";
		Map<String,String> params = new HashMap<String,String>();
//		params.put("Action", "send");
		params.put("emailAccount", receiveAccount);
		params.put("title", title);
		params.put("content", content);
		try{
			/**
			 * 经过测试，header和requestbody必须加上，否则报一大堆错误，诸如：IO错误， I/O error on POST request或者content-leng等错误
			 */
			MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
	        headers.add("Accept", "application/json");
	        headers.add("Content-Type", "application/json;charset=utf-8");
	        String requestBody = "{\"emailAccount\":\""+receiveAccount+"\",\"title\":\""+title+"\",\"content\":\""+content+"\"}";
	        HttpEntity request = new HttpEntity(requestBody, headers);
			
	        String responseText = restTemplate.postForObject(url, request,String.class, "");
	        log.info("获取响应内容："+request.getBody());
			return responseText;
			
		}catch(Exception ee){
			ee.printStackTrace();
		}
		return "failed";
		
	} 
	
	/**
	 * 查询邮件列表
	 * @return 邮件集合
	 */
	public List findEmailList(){
		Map<String,String> map = new HashMap<String,String>();
		map.put("id", "1");
		map.put("title", "我是标题");
		map.put("content", "我是内容");
		
		List list = restTemplate.getForObject(serviceUrl + "/Info/email?id={id}&title={title}&content={content}", ArrayList.class,map);
		return list;
	}
	
	
	
	/**
	 * 修改邮件信息
	 * @return
	 */
	public Email updateEmail(Email email){
		log.info("修改邮件内容....");
		
        HttpEntity<Email> request = new HttpEntity<Email>(email);//传入服务端类型为实体
        
        String result = "";
		restTemplate.exchange(serviceUrl + "/Info/email", HttpMethod.PUT, request, Email.class);
		
		log.info("值修改后："+email.getDraft());
		log.info("result:"+result);
		return email;
	}
	
	/**
	 * 查询极光信息
	 * @return
	 */
	public Jgpush findJgById(Integer id){
		log.info("查询极光信息");
		Map<String,String> map = new HashMap<String,String>();
		map.put("id", "1");
		ResponseEntity<Jgpush> jp = restTemplate.getForEntity(serviceUrl+"/Info/jgpush/{id}", Jgpush.class, map);
		return jp.getBody();
	}
	
	/**
	 * 查询极光列表
	 * @return
	 */
	public List<Jgpush> findJgpushs(){
		Map<String,String> map = new HashMap<String,String>();
		map.put("title", "标题");
		map.put("content", "内容");
		List<Jgpush> list = restTemplate.getForObject(serviceUrl+"/Info/jgpush/{content}/{content}", ArrayList.class, map);
		return list;
	}
	
	
	/**
	 * 发送极光信息
	 * @param title
	 * @param content
	 * @return
	 */
	public String sendJgpush(String title,String content,String platform,String sendId,String releaseFun){
		
		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json;charset=utf-8");
        String requestBody = "{\"platform\":\""+platform+"\",\"sendId\":\""+sendId+"\",\"releaseFun\":\""+releaseFun+"\",\"title\":\""+title+"\",\"content\":\""+content+"\"}";
        log.info("请求参数："+requestBody);
        HttpEntity request = new HttpEntity(requestBody, headers);
        String result = restTemplate.postForObject(serviceUrl+"/Info/jgpush", request,String.class);
        log.info("请求响应结果："+result);
		return result;
	}
	
	
}
