package com.own.promotion.dao.domain;

import java.util.List;

public class FullPromotion extends BasePromotion{

	
	public FullPromotion(){}
	
	private static final long serialVersionUID = 1L;
	
	private Integer basePrice;
	private Integer addPrice;
	private List<String> giftes;
	private boolean isChecked;
	
	
	
	public boolean isChecked() {
		return isChecked;
	}
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}
	public Integer getBasePrice() {
		return basePrice;
	}
	public void setBasePrice(Integer basePrice) {
		this.basePrice = basePrice;
	}
	public Integer getAddPrice() {
		return addPrice;
	}
	public void setAddPrice(Integer addPrice) {
		this.addPrice = addPrice;
	}
	public List<String> getGiftes() {
		return giftes;
	}
	public void setGiftes(List<String> giftes) {
		this.giftes = giftes;
	}
	
	

}
