package com.own.promotion.controller.bean;

import lombok.Data;

@Data
public class Clock {
	 // 用户名  
    private String userName;  
    // 是否当日生日  
    private boolean birthDay;  
    // 增加积分数目  
    private long point;  
    // 当月购物次数  
    private int buyNums;  
    // 当月退货次数  
    private int backNums;  
    // 当月购物总金额  
    private double buyMoney;  
    // 当月退货总金额  
    private double backMondy;  
    // 当月信用卡还款次数  
    private int billThisMonth;  
      
    /** 
     * 记录积分发送流水，防止重复发放 
     * @param userName 用户名 
     * @param type 积分发放类型 
     */  
    public void recordPointLog(String userName, String type){  
        System.out.println("增加对"+userName+"的类型为"+type+"的积分操作记录.");  
    }  

}
