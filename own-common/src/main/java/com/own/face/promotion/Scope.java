package com.own.face.promotion;

import lombok.Data;

import java.util.Date;

@Data
public class Scope {

    private Long id;
    private String name;
    private String scope;
    private Date createTime;
    private int flag;

}
