package com.own.face.promotion;

import lombok.Data;

import java.util.Date;

@Data
public class PromotionType {

    private Long id;
    private String saleTypeName;
    private String saleTypeAlias;
    private Date createTime;
    private String userName;
    private String userAccount;
    private int flag;
    private String saleTypeMark;
}
