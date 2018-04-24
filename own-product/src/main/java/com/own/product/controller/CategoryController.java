package com.own.product.controller;

import com.own.face.product.CategoryBean;
import com.own.face.util.Util;
import com.own.product.dao.CategoryDao;
import com.own.product.domain.Category;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by Bluce on 2018/4/4.
 */
@RestController
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryDao categoryDao;

    /**
     * 根据级别查询类别
     * @param parms
     * @return map
     */
    @RequestMapping(value="/Category",method={RequestMethod.GET})
    public @ResponseBody List<Category> queryCategory(@RequestParam Map<String,Object> parms){
        //如果没有传入级别，默认查询第一级
        String level = "1";
        if(!Util.isNullOrEmpty(parms.get("level"))){
            level = parms.get("level").toString();
        }
        List<Category> c = categoryDao.queryCategoryByLevel(level);
        return c;
    }

    /**
     * 根据id查询类别
     * @param id
     * @return
     */

    @RequestMapping(value="/Category/{id}",method={RequestMethod.GET})
    public  @ResponseBody Category queryCategoryById(@PathVariable Long id){
        Category c = categoryDao.queryCategoryById(id);
        return c;
    }

    /**
     * 添加类别
     * @param cb
     */
    @RequestMapping(value="/Category",method={RequestMethod.PUT})
    public void addCategory(@RequestBody CategoryBean cb){
        Category c = new Category();
        c.setName(cb.getName());
        c.setLevel(cb.getLevel());
        c.setRemark(cb.getRemark());
        categoryDao.save(c);
        if(!Util.isNullOrEmpty(cb.getPId())){
            categoryDao.createRelation(c.getId(),cb.getPId());
        }
    }

    @RequestMapping(value="/Category/{id}",method={RequestMethod.POST})
    public void updCategory(@PathVariable Long id){

    }

    @RequestMapping(value="/Category/{id}",method={RequestMethod.DELETE})
    public void delCategory(@PathVariable Long id){

    }
}
