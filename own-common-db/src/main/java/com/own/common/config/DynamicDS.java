package com.own.common.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 继承自 AbstractRoutingDataSource 动态数据源切换设置
 */
public class DynamicDS extends AbstractRoutingDataSource {
    @Nullable
    @Override
    protected Object determineCurrentLookupKey() {
        return DSContextHolder.getDS();
    }


    private Map<Object,Object> ds;

    public DynamicDS(){
        ds = new HashMap<>();
        super.setTargetDataSources(ds);
    }

    public <T extends DataSource> void addDS(String key,T data){ds.put(key,data);}
}
