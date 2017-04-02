package ems.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.siaya.action.core.MsgResult;
import com.siaya.exception.StateException;
import com.siaya.util.Util;

import ems.action.ConfigProperty;

public class SendSms {
	
	
	Logger logger = Logger.getLogger(SendSms.class);
	
	/**
	 * 发送短信
	 */
	public MsgResult sendSms(String mobile,String content,String serviceUrl,String sn,String pwd) {
		try{
			content = URLEncoder.encode(content+"【盛大科技】","utf-8");
			}
		catch(UnsupportedEncodingException e){
			e.printStackTrace();
			}

		MsgResult result = new MsgResult();

		String result_mt = Util.toStringAndTrim(mdsmssend(mobile,content,"","","","",serviceUrl,sn,pwd));
		logger.info("sendSms result:"+result_mt);
		if("".equals(result_mt))
			throw new StateException(result_mt,"-000002");

		if(Long.parseLong(result_mt)<0)
			throw new StateException(result_mt,"-000002");

		return result;
		
		
	}
	
	
	public String getMD5(String sourceStr) throws UnsupportedEncodingException {
		String resultStr = "";
		try {
			byte[] temp = sourceStr.getBytes();
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(temp);
			// resultStr = new String(md5.digest());
			byte[] b = md5.digest();
			for (int i = 0; i < b.length; i++) {
				char[] digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
						'9', 'A', 'B', 'C', 'D', 'E', 'F' };
				char[] ob = new char[2];
				ob[0] = digit[(b[i] >>> 4) & 0X0F];
				ob[1] = digit[b[i] & 0X0F];
				resultStr += new String(ob);
			}
			return resultStr;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getPwd(String sn,String pwd) {
		String md5pwd =null;
		try {
			md5pwd = getMD5(sn + pwd);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return md5pwd;
	}
	
	/**
	 * 发送短信接口
	 * @param mobile
	 * @param content
	 * @param ext
	 * @param stime
	 * @param rrid
	 * @param msgfmt
	 * @return
	 */
	public String mdsmssend(String mobile, String content, String ext, String stime,
			String rrid,String msgfmt,String serviceURL,String sn,String pwd){
		
		String result = "";
		String soapAction = "http://entinfo.cn/mdsmssend";
		StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		xml.append("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">");
		xml.append("<soap:Body>");
		xml.append("<mdsmssend  xmlns=\"http://entinfo.cn/\">");
		xml.append("<sn>" + sn + "</sn>");
		xml.append("<pwd>" + getPwd(sn,pwd) + "</pwd>");
		xml.append("<mobile>" + mobile + "</mobile>");
		xml.append("<content>" + content + "</content>");
		xml.append("<ext>" + ext + "</ext>");
		xml.append("<stime>" + stime + "</stime>");
		xml.append("<rrid>" + rrid + "</rrid>");
		xml.append("<msgfmt>" + msgfmt + "</msgfmt>");
		xml.append("</mdsmssend>");
		xml.append("</soap:Body>");
		xml.append("</soap:Envelope>");
		
		logger.info("发送短信，请求报文:"+xml);

		URL url;
		try {
			url = new URL(serviceURL);

			URLConnection connection = url.openConnection();
			HttpURLConnection httpconn = (HttpURLConnection) connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(xml.toString().getBytes());
			byte[] b = bout.toByteArray();
			httpconn.setRequestProperty("Content-Length", String
					.valueOf(b.length));
			httpconn.setRequestProperty("Content-Type",
					"text/xml; charset=gb2312");
			httpconn.setRequestProperty("SOAPAction", soapAction);
			httpconn.setRequestMethod("POST");
			httpconn.setDoInput(true);
			httpconn.setDoOutput(true);

			OutputStream out = httpconn.getOutputStream();
			out.write(b);
			out.close();

			InputStreamReader isr = new InputStreamReader(httpconn
					.getInputStream());
			BufferedReader in = new BufferedReader(isr);
			String inputLine;
			while (null != (inputLine = in.readLine())) {
				Pattern pattern = Pattern.compile("<mdsmssendResult>(.*)</mdsmssendResult>");
				Matcher matcher = pattern.matcher(inputLine);
				while (matcher.find()) {
					result = matcher.group(1);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/** 
     * 手机号验证 
     *  
     * @param  str 
     * @return 验证通过返回true 
     */  
    public boolean isMobile(String str) {   
        Pattern p = null;  
        Matcher m = null;  
        boolean b = false;   
        p = Pattern.compile("^[1][3,4,5,7,8][0-9]{9}$"); // 验证手机号  
        m = p.matcher(str);  
        b = m.matches();   
        return b;  
    } 
    
    
}
