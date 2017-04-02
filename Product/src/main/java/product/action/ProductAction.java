package product.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import product.dao.ProductDao;
import product.dao.domain.Product;
import product.dao.domain.Template;
import product.util.Util;
import face.core.FaceUtil;
import face.product.ProductBean;
import face.product.TemplateBean;

@EnableAutoConfiguration  
@RestController
public class ProductAction {
	
	@Autowired
	private ProductDao productDao = null;
	
	@Autowired 
	private Neo4jOperations template;
	
	/**
	 * 根据ID查询产品
	 * @param id
	 */
	@ResponseBody
	@RequestMapping(value="/Product/{id}",method={RequestMethod.GET})
	public Product getProduct(@PathVariable Long id){
		Product product = productDao.queryProductById(id);
		return product;
 	}
	
	/**
	 * 查询产品
	 * @param parms
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/Product",method={RequestMethod.GET})
	public Iterable queryProduct(@RequestParam Map<String,Object> parms){
		Iterable iterable=null;
		//List rs=new ArrayList();
		//Map<String,Object> map = new HashMap<String,Object>();
		//根据产品类型、店家查询
		if(!Util.isNullOrEmpty(parms.get("categoryId"))){
			String categoryStr = parms.get("categoryId").toString();
			String[] categoryIds = categoryStr.split(",");
			String partyId = "";
			if(!Util.isNullOrEmpty(parms.get("partyId"))){
				partyId=parms.get("partyId").toString();
			}
			String cypher = createQueryCypher(categoryIds,partyId,"product");
			 iterable=template.queryForObjects(Product.class,cypher, new HashMap());
			//rs.add(iterable);
		//	map.put("result", rs);
		}
		//根据产品id查询
		else if(!Util.isNullOrEmpty(parms.get("productId"))){
			/*List<Product> p = productDao.queryProductByIds(parms.get("productId").toString());
			map.put("result", p);*/
			StringBuffer sb = new StringBuffer();
			sb.append("start n=node(");
			sb.append(parms.get("productId").toString());
			sb.append(") return n");
			 iterable =template.queryForObjects(Product.class,sb.toString(), new HashMap());
			//map.put("result", rs);
		}
		//只根据店家查询（不包括产品类型）
		else if(!Util.isNullOrEmpty(parms.get("partyId")) && Util.isNullOrEmpty(parms.get("categoryId"))){
			StringBuffer sb = new StringBuffer();
			sb.append("match(n:Product {partyId:");
			sb.append(parms.get("partyId"));
			sb.append("}) return n");
			iterable = template.queryForObjects(Product.class,sb.toString(), new HashMap());
		//	map.put("result", rs);
		}else{
			iterable = productDao.queryProduct();
			//map.put("result", p);
		}
		//System.out.println(map.get("result"));
		return iterable;
 	}
	
	/**
	 * 通过选择多种类别查询产品，深度为0-20，类别支持最多查询26种的交集
	 * @param ids
	 * @param partyId
	 * @param judge ：Product or Template
	 * @return
	 */
	public String createQueryCypher(String[] ids,String partyId,String judge){
		StringBuffer sb = new StringBuffer();
		sb.append(" start ");
		for(int i=0;i<ids.length;i++){
			String id = ids[i];
			String letter = intToLetter(i);
			sb.append(letter+"=node("+id+")");
			if(i!=ids.length-1){
				sb.append(",");
			}
		}
		for(int i=0;i<ids.length;i++){
			String letter = intToLetter(i);
			if(i==0){
				sb.append(" match("+letter+")");
			}else{
				sb.append(" ("+letter+")");
			}
			//1. 如果judge==Product那么查询的是产品
			//2. 如果judge==Template那么查询的是模板
			sb.append("-[:category*0..20]->()-[:"+judge+"]-(p)");
			if(i!=ids.length-1){
				sb.append(",");
			}
		}
		if(!Util.isNullOrEmpty(partyId)){
			sb.append(" where p.partyId="+partyId);
		}
		sb.append(" return p ");
		return sb.toString();
	}
	
	/**
	 * 数字 to 字母   0：A , 1：B , ...... , 25：Z 
	 * @param num
	 * @return
	 */
	public String intToLetter(int num){
		int i=65;
		char letter = (char)(i+num);
		return String.valueOf(letter);
	}
	
	
	/**
	 * 添加产品
	 * @param productBean
	 */
	@RequestMapping(value="/Product",method={RequestMethod.PUT})
	public Product addProduct(@RequestBody ProductBean productBean){
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
		if(!Util.isNullOrEmpty(productBean.getProductTypeIdList()) && productBean.getProductTypeIdList().size()>0){
			List<Long> productTypeIdList = productBean.getProductTypeIdList();
			for(Long productTypeId : productTypeIdList){
				productDao.createRelation(product.getId(),productTypeId);
			}
		}
		return product;
 	}
	
	/**
	 * 修改产品
	 * @param id
	 * @param pb
	 */
	@RequestMapping(value="/Product/{id}",method={RequestMethod.POST})
	public Product updProduct(@PathVariable Long id,@RequestBody ProductBean productBean){
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
	
	/**
	 * 删除产品
	 * @param id
	 */
	@RequestMapping(value="/Product/{id}",method={RequestMethod.DELETE})
	public void delProduct(@PathVariable Long id){
		productDao.deleteProduct(id);
 	}
	
	/**
	 * 根据ID查询产品模板
	 * @param id
	 */
	@ResponseBody
	@RequestMapping(value="/Template/{id}",method={RequestMethod.GET})
	public Template getTemplate(@PathVariable Long id){
		Template templateEntity = productDao.queryTemplateById(id);
		return templateEntity;
 	}
	
	/**
	 * 查询产品模板
	 * @param parms
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/Template",method={RequestMethod.GET})
	public Iterable   queryTemplate(@RequestParam Map<String,Object> parms){
		//List<Template> templateList = new ArrayList();
		Iterable iterable =null;
		Map<String,Object> map = new HashMap<String,Object>();
		if(!Util.isNullOrEmpty(parms.get("categoryId"))){
			String categoryStr = parms.get("categoryId").toString();
			String[] categoryIds = categoryStr.split(",");
			String partyId = "";
			if(!Util.isNullOrEmpty(parms.get("partyId"))){
				partyId=parms.get("partyId").toString();
			}
			String cypher = createQueryCypher(categoryIds,partyId,"template");
			 iterable = template.queryForObjects(Template.class, cypher, new HashMap());
			//map.put("result", iterable);
		}else{
			iterable = productDao.queryTemplate();
			//map.put("result", templateList);
		}
		return iterable;
	}

	/**
	 * 添加产品模板
	 * @param templateBean
	 */
	@RequestMapping(value="/Template",method={RequestMethod.PUT})
	public Template createTemplate(@RequestBody TemplateBean templateBean){
		Map map = FaceUtil.transBean2MapNotNull(templateBean);
		Template template = productDao.createTemplate(map);
		if(!Util.isNullOrEmpty(templateBean.getProductTypeIdList()) && templateBean.getProductTypeIdList().size()>0){
			List<Long> productTypeIdList = templateBean.getProductTypeIdList();
			for(Long productTypeId : productTypeIdList){
				productDao.createTemplateRelation(template.getId(),productTypeId);
			}
		}
		return template;
	}
	
	/**
	 * 修改产品模板
	 * @param id
	 * @param templateBean
	 */
	@RequestMapping(value="/Template/{id}",method={RequestMethod.POST})
	public Template updTemplate(@PathVariable Long id,@RequestBody TemplateBean templateBean){
		Template templateEntity = productDao.queryTemplateById(id);
		templateEntity.setName(templateBean.getName());
		templateEntity.setContent(templateBean.getContent());
		template.save(templateEntity);
		return templateEntity;
 	}

}
