package com.own.common.config;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据源切换Holder
 */
@Slf4j
public class DSContextHolder {

    private static final ThreadLocal<String> dsContextHolder = new ThreadLocal<>();

    public static void setDS(String dsType){
        log.debug("切换 {}",dsType);
        dsContextHolder.set(dsType);
    }

    public static String getDS(){
        return dsContextHolder.get();
    }

    public static void clearDS(){
        dsContextHolder.remove();
    }
}
