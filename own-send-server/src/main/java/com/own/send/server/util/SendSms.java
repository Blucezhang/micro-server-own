package com.own.send.server.util;

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
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.own.face.util.Util;
import com.own.send.server.util.sms.MsgResult;
import com.own.send.server.util.sms.SmsGatewayException;


public class SendSms {
	
	
	Logger logger = Logger.getLogger(SendSms.class);
	
	/**
	 * 发送短信
	 */
	public MsgResult sendSms(String mobile,String content,String serviceUrl,String sn,String pwd) {
		if (Util.isNullOrEmpty(mobile) || Util.isNullOrEmpty(content)) {
			throw new IllegalArgumentException("mobile and content are required");
		}
		if (Util.isNullOrEmpty(serviceUrl) || Util.isNullOrEmpty(sn) || Util.isNullOrEmpty(pwd)) {
			throw new IllegalArgumentException("SMS gateway configuration is incomplete");
		}

		String encodedContent;
		try {
			encodedContent = URLEncoder.encode(content, "UTF-8");
		} catch (UnsupportedEncodingException exception) {
			throw new IllegalStateException("UTF-8 encoding is unavailable", exception);
		}

		String gatewayResponse = Util.toStringAndTrim(
				mdsmssend(mobile, encodedContent, "", "", "", "", serviceUrl, sn, pwd));
		logger.info("SMS gateway response: " + gatewayResponse);
		if (gatewayResponse.isEmpty()) {
			throw new SmsGatewayException("SMS gateway returned an empty response", gatewayResponse, "-000002");
		}

		try {
			if (Long.parseLong(gatewayResponse) < 0) {
				throw new SmsGatewayException("SMS gateway rejected the message", gatewayResponse, "-000002");
			}
		} catch (NumberFormatException exception) {
			throw new SmsGatewayException("SMS gateway returned an invalid response", gatewayResponse,
					"-000002", exception);
		}

		return MsgResult.success(gatewayResponse);
	}
	
	
	public String getMD5(String sourceStr) {
		try {
			byte[] temp = sourceStr.getBytes(StandardCharsets.UTF_8);
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			byte[] digest = md5.digest(temp);
			StringBuilder result = new StringBuilder(digest.length * 2);
			for (byte value : digest) {
				result.append(String.format("%02X", value & 0xff));
			}
			return result.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("MD5 algorithm is unavailable", e);
		}
	}
	
	public String getPwd(String sn,String pwd) {
		return getMD5(sn + pwd);
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
		//http://entinfo.cn/mdsmssend
		String result = "";
		String soapAction = "http://entinfo.cn/mdsmssend";
		StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		xml.append("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">");
		xml.append("<soap:Body>");
		xml.append("<mdsmssend  xmlns=\"http://entinfo.cn/\">");
		xml.append("<sn>").append(escapeXml(sn)).append("</sn>");
		xml.append("<pwd>").append(escapeXml(getPwd(sn, pwd))).append("</pwd>");
		xml.append("<mobile>").append(escapeXml(mobile)).append("</mobile>");
		xml.append("<content>").append(escapeXml(content)).append("</content>");
		xml.append("<ext>").append(escapeXml(ext)).append("</ext>");
		xml.append("<stime>").append(escapeXml(stime)).append("</stime>");
		xml.append("<rrid>").append(escapeXml(rrid)).append("</rrid>");
		xml.append("<msgfmt>").append(escapeXml(msgfmt)).append("</msgfmt>");
		xml.append("</mdsmssend>");
		xml.append("</soap:Body>");
		xml.append("</soap:Envelope>");
		
		logger.info("Sending SMS request to gateway: " + serviceURL);

		URL url;
		HttpURLConnection httpconn = null;
		try {
			url = new URL(serviceURL);

			URLConnection connection = url.openConnection();
			httpconn = (HttpURLConnection) connection;
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			bout.write(xml.toString().getBytes(StandardCharsets.UTF_8));
			byte[] b = bout.toByteArray();
			httpconn.setRequestProperty("Content-Length", String
					.valueOf(b.length));
			httpconn.setRequestProperty("Content-Type",
					"text/xml; charset=UTF-8");
			httpconn.setRequestProperty("SOAPAction", soapAction);
			httpconn.setRequestMethod("POST");
			httpconn.setDoInput(true);
			httpconn.setDoOutput(true);
			httpconn.setConnectTimeout(5000);
			httpconn.setReadTimeout(10000);

			try (OutputStream out = httpconn.getOutputStream()) {
				out.write(b);
			}

			try (BufferedReader in = new BufferedReader(new InputStreamReader(httpconn.getInputStream(), StandardCharsets.UTF_8))) {
				String inputLine;
				while (null != (inputLine = in.readLine())) {
					Pattern pattern = Pattern.compile("<mdsmssendResult>(.*)</mdsmssendResult>");
					Matcher matcher = pattern.matcher(inputLine);
					while (matcher.find()) {
						result = matcher.group(1);
					}
				}
			}
			return result;
		} catch (Exception e) {
			throw new SmsGatewayException("SMS gateway request failed", "", "-000003", e);
		} finally {
			if (httpconn != null) httpconn.disconnect();
		}
	}

	private String escapeXml(String value) {
		return Util.toStringAndTrim(value)
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&apos;");
	}
	
	/** 
     * 手机号验证 
     *  
     * @param  str 
     * @return 验证通过返回true 
     */  
    public boolean isMobile(String str) {   
        return str != null && Pattern.compile("^1[3-9][0-9]{9}$").matcher(str).matches();
    } 
    
    
}
