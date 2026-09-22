package com.own.order.service;

import com.own.face.security.InternalServiceGuard;
import com.own.face.trade.TradeException;
import java.util.Map;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

@Component public class AddressClient {
 private final RestTemplate restTemplate; private final String internalToken; public AddressClient(RestTemplate restTemplate,@Value("${trade.internal.service-token:}") String internalToken){this.restTemplate=restTemplate;this.internalToken=internalToken;}
 @SuppressWarnings("unchecked") public AddressSnapshot get(Long buyerId, Long addressId) {
  if(addressId==null || addressId.longValue()<=0) throw TradeException.unprocessable("addressId is required");
  HttpHeaders headers=new HttpHeaders(); headers.set("X-Actor-Id", "0"); headers.set("X-Actor-Type", "SYSTEM"); headers.set(InternalServiceGuard.HEADER,internalToken);
  Map response=restTemplate.exchange("http://own-user-party/api/v1/internal/buyers/{buyerId}/addresses/{addressId}", HttpMethod.GET, new HttpEntity<Void>(headers), Map.class, buyerId,addressId).getBody();
  if(response==null || response.get("data")==null) throw TradeException.unprocessable("address was not found");
  Map data=(Map)response.get("data"); return new AddressSnapshot(addressId, str(data,"recipientName"),str(data,"mobile"),str(data,"province"),str(data,"city"),str(data,"district"),str(data,"detail"));
 }
 private String str(Map data,String key){Object value=data.get(key);if(value==null)throw TradeException.unprocessable("address is incomplete");return String.valueOf(value);}
 public static class AddressSnapshot { public final Long id; public final String recipientName,mobile,province,city,district,detail; AddressSnapshot(Long id,String recipientName,String mobile,String province,String city,String district,String detail){this.id=id;this.recipientName=recipientName;this.mobile=mobile;this.province=province;this.city=city;this.district=district;this.detail=detail;} public String formatted(){return province+city+district+detail;} }
}
