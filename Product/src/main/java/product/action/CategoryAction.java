package product.action;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import product.dao.CategoryDao;
import product.dao.domain.Category;
import product.util.Util;
import face.product.CategoryBean;

@EnableAutoConfiguration  
@RestController 
public class CategoryAction {
	
	@Autowired
	private CategoryDao categoryDao;
	
	/**
	 * 根据级别查询类别
	 * @param parms
	 * @return map
	 */
	@ResponseBody
	@RequestMapping(value="/Category",method={RequestMethod.GET})
	public List<Category> queryCategory(@RequestParam Map<String,Object> parms){
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
	@ResponseBody
	@RequestMapping(value="/Category/{id}",method={RequestMethod.GET})
	public Category queryCategoryById(@PathVariable Long id){
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
		if(!Util.isNullOrEmpty(cb.getpId())){
			categoryDao.createRelation(c.getId(),cb.getpId());
		}
 	}
	
	@RequestMapping(value="/Category/{id}",method={RequestMethod.POST})
	public void updCategory(@PathVariable Long id){
		
 	}
	
	@RequestMapping(value="/Category/{id}",method={RequestMethod.DELETE})
	public void delCategory(@PathVariable Long id){
		
 	}

}
