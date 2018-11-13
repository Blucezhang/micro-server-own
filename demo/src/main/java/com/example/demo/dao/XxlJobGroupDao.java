package com.example.demo.dao;


import com.example.demo.entity.XxlJobGroup;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * Created by xuxueli on 16/9/30.
 */
@Mapper
public interface XxlJobGroupDao {

    public List<XxlJobGroup> findAll();

    List<XxlJobGroup> queryAll();
}
