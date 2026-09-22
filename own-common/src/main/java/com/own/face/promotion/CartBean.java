package com.own.face.promotion;


import com.own.face.core.FaceUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.Data;

//购物结算时，处理参数bean
@Data
public class CartBean {
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	
	/**
	 * 1.套装促销类型：productJson为json数组字符串，里面包含商品等信息；此时singlePrice为单个套装价格
	 */
	private String productId;
	private String productName;
	private Double singlePrice;
	private Integer amount;//数量
	private Double total;//原始计算总价
	private Double afterTotal;//计算后的总价
	private String productJson;//商品其他属性，如果是套装促销，此处传套装商品信息，如：[{productId:1,price:12,count:2,order:1},{productId:2,price:12,count:2,order:2}]
	private String promotionId;
	private Long promotionTypeId;//活动类型id 
	private boolean join;//是否参加
	private boolean selectedGifts;//是否选择赠品
	private Double productTotolCount;

	
	public Double doSome(CartBean c){
		String productJson = c.getProductJson();
		Integer conuters = 0;
		Double signle = 0d;
		if (!FaceUtil.isNullOrEmpty(productJson) && productJson.startsWith("[") && productJson.endsWith("]")) {
			try {
				JsonNode json = OBJECT_MAPPER.readTree(productJson);
				if (json != null && json.isArray() && json.size() > 0) {
					for (JsonNode job : json) {
						conuters = job.path("count").asInt();
						signle += conuters * job.path("price").asDouble();
					}
				}
				} catch (IOException exception) {
				throw new IllegalArgumentException("productJson must be a valid JSON array", exception);
			}
			
		}
		return signle;
	}
	
	
	
}
