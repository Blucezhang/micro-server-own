package com.own.product.controller;

import com.own.face.core.FaceUtil;
import com.own.face.product.ProductBean;
import com.own.face.product.TemplateBean;
import com.own.face.util.Util;
import com.own.product.dao.ProductDao;
import com.own.product.domain.Product;
import com.own.product.domain.Template;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bluce on 2018/4/4.
 */
@Slf4j
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductDao productDao = null;
    @Autowired
    private Neo4jOperations template;

    @ApiOperation(value = "根据ID查询产品")
    @GetMapping("/Product/{id}")
    public @ResponseBody Product getProduct(@PathVariable Long id) {
        Product product = productDao.queryProductById(id);
        return product;
    }


    @ApiOperation(value = "查询产品")
    @ApiImplicitParam(value = "Bean参数",dataType = "Map")
    @GetMapping("")
    public @ResponseBody Iterable queryProduct(@RequestParam Map<String, Object> parms) {
        Iterable iterable = null;
        //根据产品类型、店家查询
        if (!Util.isNullOrEmpty(parms.get("categoryId"))) {
            String categoryStr = parms.get("categoryId").toString();
            String[] categoryIds = categoryStr.split(",");
            String partyId = "";
            if (!Util.isNullOrEmpty(parms.get("partyId"))) {
                partyId = parms.get("partyId").toString();
            }
            String cypher = createQueryCypher(categoryIds, partyId, "product");
            iterable = template.queryForObjects(Product.class, cypher, new HashMap());
        }
        //根据产品id查询
        else if (!Util.isNullOrEmpty(parms.get("productId"))) {
            StringBuffer sb = new StringBuffer();
            sb.append("start n=node(");
            sb.append(parms.get("productId").toString());
            sb.append(") return n");
            iterable = template.queryForObjects(Product.class, sb.toString(), new HashMap());
        }
        //只根据店家查询（不包括产品类型）
        else if (!Util.isNullOrEmpty(parms.get("partyId")) && Util.isNullOrEmpty(parms.get("categoryId"))) {
            StringBuffer sb = new StringBuffer();
            sb.append("match(n:Product {partyId:");
            sb.append(parms.get("partyId"));
            sb.append("}) return n");
            iterable = template.queryForObjects(Product.class, sb.toString(), new HashMap());
        } else {
            iterable = productDao.queryProduct();
        }
        return iterable;
    }

    /**
     * 通过选择多种类别查询产品，深度为0-20，类别支持最多查询26种的交集
     *
     * @param ids
     * @param partyId
     * @param judge   ：Product or Template
     * @return
     */
    public String createQueryCypher(String[] ids, String partyId, String judge) {
        StringBuffer sb = new StringBuffer();
        sb.append(" start ");
        for (int i = 0; i < ids.length; i++) {
            String id = ids[i];
            String letter = intToLetter(i);
            sb.append(letter + "=node(" + id + ")");
            if (i != ids.length - 1) {
                sb.append(",");
            }
        }
        for (int i = 0; i < ids.length; i++) {
            String letter = intToLetter(i);
            if (i == 0) {
                sb.append(" match(" + letter + ")");
            } else {
                sb.append(" (" + letter + ")");
            }
            //1. 如果judge==Product那么查询的是产品
            //2. 如果judge==Template那么查询的是模板
            sb.append("-[:category*0..20]->()-[:" + judge + "]-(p)");
            if (i != ids.length - 1) {
                sb.append(",");
            }
        }
        if (!Util.isNullOrEmpty(partyId)) {
            sb.append(" where p.partyId=" + partyId);
        }
        sb.append(" return p ");
        return sb.toString();
    }

    /**
     * 数字 to 字母   0：A , 1：B , ...... , 25：Z
     *
     * @param num
     * @return
     */
    public String intToLetter(int num) {
        int i = 65;
        char letter = (char) (i + num);
        return String.valueOf(letter);
    }

    @ApiOperation(value = "添加产品")
    @PutMapping("")
    public Product addProduct(@RequestBody ProductBean productBean) {
        Product product = new Product();
        product.setName(productBean.getName());
        product.setContent(productBean.getContent());
        product.setPartyId(productBean.getPartyId());
        product.setOriginalPrice(productBean.getOriginalPrice());
        product.setPromotionPrice(productBean.getPromotionPrice());
        product.setStocksNum(productBean.getStocksNum());
        product.setSalesNum(productBean.getSalesNum());
        product.setCategoryId(productBean.getCategoryId());
        product.setBrandId(productBean.getBrandId());
        productDao.save(product);
        if (!Util.isNullOrEmpty(productBean.getProductTypeIdList()) && productBean.getProductTypeIdList().size() > 0) {
            List<Long> productTypeIdList = productBean.getProductTypeIdList();
            for (Long productTypeId : productTypeIdList) {
                productDao.createRelation(product.getId(), productTypeId);
            }
        }
        return product;
    }

    @ApiOperation(value = "修改产品")
    @PostMapping("/{id}")
    public Product updProduct(@PathVariable Long id, @RequestBody ProductBean productBean) {
        Product product = productDao.queryProductById(id);
        product.setName(productBean.getName());
        product.setContent(productBean.getContent());

        product.setPartyId(productBean.getPartyId());
        product.setOriginalPrice(productBean.getOriginalPrice());
        product.setPromotionPrice(productBean.getPromotionPrice());
        product.setStocksNum(productBean.getStocksNum());
        product.setSalesNum(productBean.getSalesNum());
        product.setCategoryId(productBean.getCategoryId());
        product.setBrandId(productBean.getBrandId());

        return productDao.save(product);
    }

    @ApiOperation(value = "根据ID删除产品")
    @DeleteMapping("/{id}")
    public void delProduct(@PathVariable Long id) {
        productDao.deleteProduct(id);
    }

    @ApiOperation(value = "根据ID查询产品模板")
    @GetMapping("/template/{id}")
    public @ResponseBody Template getTemplate(@PathVariable Long id) {
        Template templateEntity = productDao.queryTemplateById(id);
        return templateEntity;
    }

    @ApiOperation(value = "查询产品模板")
    @GetMapping("/template")
    public @ResponseBody Iterable queryTemplate(@RequestParam Map<String, Object> parms) {
        Iterable iterable = null;
        Map<String, Object> map = new HashMap<String, Object>();
        if (!Util.isNullOrEmpty(parms.get("categoryId"))) {
            String categoryStr = parms.get("categoryId").toString();
            String[] categoryIds = categoryStr.split(",");
            String partyId = "";
            if (!Util.isNullOrEmpty(parms.get("partyId"))) {
                partyId = parms.get("partyId").toString();
            }
            String cypher = createQueryCypher(categoryIds, partyId, "template");
            iterable = template.queryForObjects(Template.class, cypher, new HashMap());
        } else {
            iterable = productDao.queryTemplate();
        }
        return iterable;
    }

    @ApiOperation(value = "添加产品模板")
    @PutMapping("/template")
    public Template createTemplate(@RequestBody TemplateBean templateBean) {
        Map map = FaceUtil.transBean2MapNotNull(templateBean);
        Template template = productDao.createTemplate(map);
        if (!Util.isNullOrEmpty(templateBean.getProductTypeIdList()) && templateBean.getProductTypeIdList().size() > 0) {
            List<Long> productTypeIdList = templateBean.getProductTypeIdList();
            for (Long productTypeId : productTypeIdList) {
                productDao.createTemplateRelation(template.getId(), productTypeId);
            }
        }
        return template;
    }

    @ApiOperation(value = "修改产品模板")
    @PostMapping("/template/{id}")
    public Template updTemplate(@PathVariable Long id, @RequestBody TemplateBean templateBean) {
        Template templateEntity = productDao.queryTemplateById(id);
        templateEntity.setName(templateBean.getName());
        templateEntity.setContent(templateBean.getContent());
        template.save(templateEntity);
        return templateEntity;
    }

}
