package face.promotion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import face.core.FaceBase;
import face.core.IfException;

@Service
public class PromotionFace extends FaceBase{

	protected String serviceUrl="//PROMOTIONWJJ/sale";
	
	Logger log = Logger.getLogger(PromotionFace.class);
	
	
	/**
	 * 查询活动信息
	 * @param id
	 * @return
	 */
	public Promotion getPromotionById(Integer id) throws IfException{
		ResponseEntity<Promotion> re = restTemplate.getForEntity(serviceUrl+"/promotion/{id}", Promotion.class, id);
		return re.getBody();
	}
	
	
	/**
	 * 保存活动数据(单品销售：直降、折扣)
	 * @param p
	 * @return
	 */
//	public Promotion savePromotion(Promotion p) throws IfException{
//		
//		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
//        headers.add("Accept", "application/json");
//        headers.add("Content-Type", "application/json;charset=utf-8");
//        HttpEntity request = new HttpEntity(p, headers);
//        Map<String,Object> param = new HashMap<String,Object>();
//        param.put("promotionType", "discount");
//        param.put("bean", p);
//		
//        ResponseEntity<Promotion> re = restTemplate.postForEntity(serviceUrl+"/promotion", request, Promotion.class,param);
//		return re.getBody();
//	}
	
	/**
	 * 保存活动数据(单品销售：直降、折扣)
	 * @param p
	 * @return
	 */
	public Map<String,Object> savePromotion(Map<String,Object> map) throws IfException{
		
		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json;charset=utf-8");
        HttpEntity request = new HttpEntity(map, headers);
		
        ResponseEntity<Map> re = restTemplate.postForEntity(serviceUrl+"/promotion", request, Map.class);
		return re.getBody();
	}
	
	
	/**
	 * 查询活动列表信息
	 * @return
	 */
	public List<Promotion> findPromotions() throws IfException{
		List<Promotion> list = restTemplate.getForObject(serviceUrl+"/promotion", ArrayList.class, "");
		return list;
	}
	
	/**
	 * 修改活动信息
	 * @param p
	 * @return
	 */
	public Promotion upPromotion(Promotion p) throws IfException{
		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json;charset=utf-8");
        String requestBody = "{\"saleName\":\""+p.getSaleName()+"\"}";
        HttpEntity request = new HttpEntity(requestBody, headers);
		
		return put(serviceUrl+"/promotion",request, Promotion.class);
	}
	
	
	
	/**
	 * 删除活动数据
	 * @param id
	 */
	public void deletePromotion(Integer id) throws IfException{
		restTemplate.delete(serviceUrl+"/promotion/{id}", id);
	}
	
	
	//=================================================================促销类型
	
	/**
	 * 查询活动类型信息
	 * @param id
	 * @return
	 */
	public PromotionType getPromotionTypeById(Integer id) throws IfException{
		ResponseEntity<PromotionType> re = restTemplate.getForEntity(serviceUrl+"/promotionType/{id}", PromotionType.class, id);
		return re.getBody();
	}
	
	
	/**
	 * 保存活动类型数据
	 * @param p
	 * @return
	 */
	public PromotionType savePromotionType(PromotionType p) throws IfException{
		
		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json;charset=utf-8");
        String requestBody = "{\"saleTypeName\":\""+p.getSaleTypeName()+"\",\"saleTypeAlias\":\""+p.getSaleTypeAlias()+"\",\"saleTypeMark\":\""+p.getSaleTypeMark()+"\"}";
        HttpEntity request = new HttpEntity(requestBody, headers);
		
        ResponseEntity<PromotionType> re = restTemplate.postForEntity(serviceUrl+"/promotionType", request, PromotionType.class);
		return re.getBody();
	}
	
	/**
	 * 查询活动类型列表信息
	 * @return
	 */
	public List<PromotionType> findPromotionTypes() throws IfException{
		List<PromotionType> list = restTemplate.getForObject(serviceUrl+"/promotionType", ArrayList.class, "");
		return list;
	}
	
	/**
	 * 修改活动类型信息
	 * @param p
	 * @return
	 */
	public PromotionType upPromotionType(PromotionType p) throws IfException{
		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json;charset=utf-8");
        String requestBody = "{\"saleTypeName\":\""+p.getSaleTypeName()+"\"}";
        HttpEntity request = new HttpEntity(requestBody, headers);
		
		return put(serviceUrl+"/promotionType",request, PromotionType.class);
	}
	
	
	
	/**
	 * 删除活动类型数据
	 * @param id
	 */
	public void deletePromotionType(Integer id) throws IfException{
		restTemplate.delete(serviceUrl+"/promotionType/{id}", id);
	}
	
	//=================================================================促销范围
	
	/**
	 * 查询范围信息
	 * @param id
	 * @return
	 */
	public Scope getScopeById(Integer id) throws IfException{
		ResponseEntity<Scope> re = restTemplate.getForEntity(serviceUrl+"/scope/{id}", Scope.class, id);
		return re.getBody();
	}
	
	
	/**
	 * 保存范围数据
	 * @param p
	 * @return
	 */
	public Scope saveScope(Scope s) throws IfException{
		
		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json;charset=utf-8");
        String requestBody = "{\"name\":\""+s.getName()+"\",\"scope\":\""+s.getScope()+"\"}";
        HttpEntity request = new HttpEntity(requestBody, headers);
		
        ResponseEntity<Scope> re = restTemplate.postForEntity(serviceUrl+"/scope", request, Scope.class);
		return re.getBody();
	}
	
	/**
	 * 查询范围列表信息
	 * @return
	 */
	public List<Scope> findScopes() throws IfException{
		List<Scope> list = restTemplate.getForObject(serviceUrl+"/scope", ArrayList.class, "");
		return list;
	}
	
	/**
	 * 修改范围信息
	 * @param p
	 * @return
	 */
	public Scope upScope(Scope s) throws IfException{
		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json;charset=utf-8");
        String requestBody = "{\"name\":\""+s.getName()+"\"}";
        HttpEntity request = new HttpEntity(requestBody, headers);
		
		return put(serviceUrl+"/scope",request, Scope.class);
	}
	
	
	
	/**
	 * 删除范围数据
	 * @param id
	 */
	public void deleteScope(Integer id) throws IfException{
		restTemplate.delete(serviceUrl+"/scope/{id}", id);
	}
	
	
	
	
	
	
	//=================================================================商城券
	
	/**
	 * 查询范围信息
	 * @param id
	 * @return
	 */
	public MallTicket getMallTicketById(Integer id) throws IfException{
		ResponseEntity<MallTicket> re = restTemplate.getForEntity(serviceUrl+"/mallTicket/{id}", MallTicket.class, id);
		return re.getBody();
	}
	
	
	/**
	 * 保存范围数据
	 * @param p
	 * @return
	 */
	public MallTicket saveMallTicket(MallTicket s) throws IfException{
		
		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json;charset=utf-8");
        String requestBody = "{\"ticketName\":\""+s.getTicketName()+"\"}";
        HttpEntity request = new HttpEntity(requestBody, headers);
		
        ResponseEntity<MallTicket> re = restTemplate.postForEntity(serviceUrl+"/mallTicket", request, MallTicket.class);
		return re.getBody();
	}
	
	/**
	 * 查询范围列表信息
	 * @return
	 */
	public List<MallTicket> findMallTickets() throws IfException{
		List<MallTicket> list = restTemplate.getForObject(serviceUrl+"/mallTicket", ArrayList.class, "");
		return list;
	}
	
	/**
	 * 修改范围信息
	 * @param p
	 * @return
	 */
	public MallTicket upMallTicket(MallTicket s) throws IfException{
		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json;charset=utf-8");
        String requestBody = "{\"ticketName\":\""+s.getTicketName()+"\"}";
        HttpEntity request = new HttpEntity(requestBody, headers);
		
		return put(serviceUrl+"/mallTicket",request, MallTicket.class);
	}
	
	
	
	/**
	 * 删除范围数据
	 * @param id
	 */
	public void deleteMallTicket(Integer id) throws IfException{
		restTemplate.delete(serviceUrl+"/mallTicket/{id}", id);
	}
	
	
	
	
	//========================================================店铺券
	
	/**
	 * 查询店铺券信息
	 * @param id
	 * @return
	 */
	public StoreTicket getStoreTicketById(Integer id) throws IfException{
		ResponseEntity<StoreTicket> re = restTemplate.getForEntity(serviceUrl+"/storeTicket/{id}", StoreTicket.class, id);
		return re.getBody();
	}
	
	
	/**
	 * 保存店铺券数据
	 * @param p
	 * @return
	 */
	public StoreTicket saveStoreTicket(StoreTicket s) throws IfException{
		
		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json;charset=utf-8");
        String requestBody = "{\"ticketName\":\""+s.getTicketName()+"\"}";
        HttpEntity request = new HttpEntity(requestBody, headers);
		
        ResponseEntity<StoreTicket> re = restTemplate.postForEntity(serviceUrl+"/storeTicket", request, StoreTicket.class);
		return re.getBody();
	}
	
	/**
	 * 查询店铺券列表信息
	 * @return
	 */
	public List<StoreTicket> findStoreTickets() throws IfException{
		List<StoreTicket> list = restTemplate.getForObject(serviceUrl+"/storeTicket", ArrayList.class, "");
		return list;
	}
	
	/**
	 * 修改店铺券信息
	 * @param p
	 * @return
	 */
	public StoreTicket upMallTicket(StoreTicket s) throws IfException{
		MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
        headers.add("Accept", "application/json");
        headers.add("Content-Type", "application/json;charset=utf-8");
        String requestBody = "{\"ticketName\":\""+s.getTicketName()+"\"}";
        HttpEntity request = new HttpEntity(requestBody, headers);
		
		return put(serviceUrl+"/storeTicket",request, StoreTicket.class);
	}
	
	
	/**
	 * 删除店铺券
	 * @param id
	 */
	public void deleteStoreTicket(Integer id) throws IfException{
		restTemplate.delete(serviceUrl+"/storeTicket/{id}", id);
	}
	
	
	
	//========================================================设定参加活动的产品
	
		/**
		 * 查询参加活动商品信息
		 * @param id
		 * @return
		 */
		public Product getProductById(Integer id) throws IfException{
			ResponseEntity<Product> re = restTemplate.getForEntity(serviceUrl+"/product/{id}", Product.class, id);
			return re.getBody();
		}
		
		
		/**
		 * 设定参加活动的商品
		 * @param p
		 * @return
		 */
		public Product saveProduct(Product s) throws IfException{
			
			MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
	        headers.add("Accept", "application/json");
	        headers.add("Content-Type", "application/json;charset=utf-8");
	        String requestBody = "{\"productName\":\""+s.getProductName()+"\"}";
	        HttpEntity request = new HttpEntity(requestBody, headers);
			
	        ResponseEntity<Product> re = restTemplate.postForEntity(serviceUrl+"/product", request, Product.class);
			return re.getBody();
		}
		
		/**
		 * 查询参加活动商品列表
		 * @return
		 */
		public List<Product> findProducts() throws IfException{
			List<Product> list = restTemplate.getForObject(serviceUrl+"/product", ArrayList.class, "");
			return list;
		}
		
		/**
		 * 修改参加活动商品信息
		 * @param p
		 * @return
		 */
		public Product upProduct(Product s) throws IfException{
			MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
	        headers.add("Accept", "application/json");
	        headers.add("Content-Type", "application/json;charset=utf-8");
	        String requestBody = "{\"productName\":\""+s.getProductName()+"\"}";
	        HttpEntity request = new HttpEntity(requestBody, headers);
			
			return put(serviceUrl+"/product",request, Product.class);
		}
		
		
		/**
		 * 删除参加活动的商品
		 * @param id
		 */
		public void deleteProduct(Integer id) throws IfException{
			restTemplate.delete(serviceUrl+"/product/{id}", id);
		}
		
		
		
		/**
		 * 设置参与活动的卖家
		 * @param p
		 * @return
		 */
		public Seller saveSeller(Seller s) throws IfException{
			MultiValueMap<String, Object> headers = new LinkedMultiValueMap<String, Object>();
	        headers.add("Accept", "application/json");
	        headers.add("Content-Type", "application/json;charset=utf-8");
	        String requestBody = "{\"sellerName\":\""+s.getSellerName()+"\",\"sellerId\":\""+s.getSellerId()+"\"}";
	        HttpEntity request = new HttpEntity(requestBody, headers);
			
			ResponseEntity<Seller> re = restTemplate.postForEntity(serviceUrl+"/promotion/seller", request, Seller.class);
			return re.getBody();
			
		}
		
		
		/**
		 * 根据商品id，查询对应促销信息
		 * @return
		 */
		public List findPromotionByProductInfo(String productId) throws IfException{
			Map map = new HashMap();
			map.put("productId",productId);
			List list = restTemplate.getForObject(serviceUrl+"/promotion/productPromotion?productId={productId}", ArrayList.class, map);
			return list;
		}
		
		/**
		 * 根据卖家id，查询对应促销信息
		 * @return
		 */
		public List<Promotion> findPromotionBySellerInfo() throws IfException{
			Map map = new HashMap();
			map.put("sellerId", "");
			List<Promotion> list = restTemplate.getForObject(serviceUrl+"/promotion/sellerPromotion?sellerId={sellerId}", ArrayList.class, map);
			return list;
		}
		
		/**
		 * 根据商品类型，查询对应促销信息
		 * @return
		 */
		public List<Promotion> findPromotionByTypeInfo() throws IfException{
			Map map = new HashMap();
			map.put("typeId", "");
			List<Promotion> list = restTemplate.getForObject(serviceUrl+"/promotion/sellerPromotion?typeId={typeId}", ArrayList.class, map);
			return list;
		}
		
		
		/**
		 * 根据地域，查询对应促销信息
		 * @return
		 */
		public List<Promotion> findPromotionByZoneInfo() throws IfException{
			Map map = new HashMap();
			map.put("zoneId", "");
			List<Promotion> list = restTemplate.getForObject(serviceUrl+"/promotion/sellerPromotion?zoneId={zoneId}", ArrayList.class, map);
			return list;
		}
		
		
		
		/**
		 * 购物结算
		 * 公共方法，使用所有促销类型
		 * @param p
		 * @return
		 */
		public CartBean calculate(CartBean cb) throws IfException{
			return get(serviceUrl+"/promotion/calculate?productId={productId}&promotionId={promotionId}&promotionTypeId={promotionTypeId}&join={join}&amount={amount}&singlePrice={singlePrice}&selectedGifts={selectedGifts}&productName={productName}&productJson={productJson}&productTotolCount={productTotolCount}", CartBean.class, cb);
		}
		
}
