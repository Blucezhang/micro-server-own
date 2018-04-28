package com.own.promotion.dao.domain;

import lombok.Data;

/**
 * 赠品促销
 */
@Data
public class PromotionalGifts extends BasePromotion {
	
	private String giftContent;//存储序列化的赠品信息
}
