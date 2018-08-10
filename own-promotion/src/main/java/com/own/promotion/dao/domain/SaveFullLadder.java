package com.own.promotion.dao.domain;


import lombok.Data;

/**
 * 阶梯满减 
 */
@Data
public class SaveFullLadder extends BasePromotion {
	
	private Integer baseCoutOneLevel;//一级 满多少
	private Integer baseCutOneLevel;//一级 减多少
	private Integer baseCoutTwoLevel;//二级 满多少
	private Integer baseCutTwoLevel;//二级 减多少
	private Integer baseCoutThreeLevel;//三级 满多少
	private Integer baseCutThreeLevel;//三级 减多少

}
