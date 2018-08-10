package com.own.product.controller;

import com.own.face.product.CategoryBean;
import com.own.face.util.Util;
import com.own.face.util.base.BaseController;
import com.own.product.dao.CategoryDao;
import com.own.product.domain.Category;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by Bluce on 2018/4/4.
 */
@Slf4j
@RestController
@RequestMapping("/category")
public class CategoryController extends BaseController {

    @Autowired
    private CategoryDao categoryDao;

    @ApiOperation(value = "根据级别查询类别")
    @GetMapping("/query")
    public @ResponseBody List<Category> queryCategory(@RequestParam Map<String,Object> parms){
        //如果没有传入级别，默认查询第一级
        String level = "1";
        if(!Util.isNullOrEmpty(parms.get("level"))){
            level = parms.get("level").toString();
        }
        List<Category> c = categoryDao.queryCategoryByLevel(level);
        return c;
    }

    @ApiOperation(value = "根据id查询类别")
    @GetMapping("/{id}")
    public  @ResponseBody Category queryCategoryById(@PathVariable Long id){
        Category c = categoryDao.queryCategoryById(id);
        log.info("category:{}",c);
        return c;
    }

    @ApiOperation(value = "添加类别")
    @PutMapping("/add")
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

    @ApiOperation(value = "根据Id修改类别信息")
    @PostMapping("/{id}")
    public void updCategory(@PathVariable Long id,@RequestBody CategoryBean categoryBean){
      Category category= categoryDao.queryCategoryById(id);
      categoryDao.save(new Category(){
          {
              setId(category.getId());
              setLevel(categoryBean.getLevel());
              setName(categoryBean.getName());
              setRemark(categoryBean.getRemark());
          }
      });
    }
    @ApiOperation(value = "根据ID删除类别")
    @DeleteMapping("/{id}")
    public void delCategory(@PathVariable Long id){
        categoryDao.deleteCategory(id);
    }
}
