package promotion.action;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import face.core.FaceUtil;
import face.core.IfException;
import face.product.ProductBean;
import face.product.ProductFace;
import face.promotion.CartBean;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import promotion.action.bean.Clock;
import promotion.action.bean.SellerBean;
import promotion.dao.MallTicketDao;
import promotion.dao.ProductDao;
import promotion.dao.PromotionDao;
import promotion.dao.SellerDao;
import promotion.dao.StoreTicketDao;
import promotion.dao.domain.FullCut;
import promotion.dao.domain.FullPromotion;
import promotion.dao.domain.NmPromotion;
import promotion.dao.domain.Product;
import promotion.dao.domain.Promotion;
import promotion.dao.domain.PromotionalGifts;
import promotion.dao.domain.SaveFullLadder;
import promotion.dao.domain.Seller;
import promotion.dao.domain.SetPromotion;

@RestController
@RequestMapping(value = "/sale/promotion")
public class PromotionAction {
	
	@Autowired
	private PromotionDao promotionDao;

	@Autowired
	private SellerDao sellerDao;

	@Autowired
	private ProductDao productDao;
	@Autowired
	private MallTicketDao mallTicketDao;
	@Autowired
	private StoreTicketDao storeTicketDao;

	Logger log = Logger.getLogger(PromotionAction.class);

	/**
	 * 查询单条活动信息
	 * 
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{id}", method = { RequestMethod.GET })
	public Promotion findPromotionById(@PathVariable Integer id) {
		log.info("查询id为：" + id + "的活动");
		// return promotionDao.findPromotion(id.longValue());
		return (Promotion) promotionDao.getFromId(id);
	}

	/**
	 * 查询活动列表，支持搜索
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "", method = { RequestMethod.GET })
	public List<Promotion> findAll() {
		List<Promotion> list = new ArrayList<Promotion>();
		list = promotionDao.findAllPromotion();
		return list;
	}

	/**
	 * 保存活动信息，此处暂时先实现单品促销，对于别的促销方式，后续采用java引擎规则实现
	 * 
	 * @param promotionbean
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "", method = { RequestMethod.POST })
	public Map<String, Object> save(@RequestBody Map param) {
		Integer typeId = Integer.valueOf(FaceUtil.toStringAndTrim(param.get("promotionTypeId")));

		Map<String, Object> result = new HashMap<String, Object>();

		if (typeId == 12) {//折扣
			Promotion promotion = new Promotion();
			promotion = (Promotion)FaceUtil.transformMap2Bean(param, promotion);
			
			promotion.setFlag(0);//默认
			promotion.setSaleStatus(0);//默认
			promotion.setCreateTime(new Date().toLocaleString());//日期
			
			Promotion pro = (Promotion)promotionDao.save(promotion);
			createPromotion(Integer.valueOf(FaceUtil.toStringAndTrim(pro.getId())), 
					typeId, 
					Integer.valueOf(FaceUtil.toStringAndTrim(param.get("scopeId"))), 
					FaceUtil.toStringAndTrim(param.get("productJson")));
			String mallTicketId =  param.get("mallTicketId")!=null?param.get("mallTicketId").toString():null;
			String storeTicketId = param.get("storeTicketId")!=null?param.get("storeTicketId").toString():null;
			
			//商城券与活动建立关系
			if(mallTicketId!=null && !mallTicketId.equals("null") && mallTicketId!="null" && !mallTicketId.equals("")){
				mallTicketDao.createRelationship(Integer.valueOf(mallTicketId), pro.getId().intValue(), "BELONG");
			}
			//店铺券与活动建立关系
			if(storeTicketId!=null && !storeTicketId.equals("null") && storeTicketId!="null" && !storeTicketId.equals("")){
				storeTicketDao.createRelationship(Integer.valueOf(storeTicketId), pro.getId().intValue(), "BELONG");
			}
			log.info("活动信息："+promotion.getId()+"  "+promotion.getSaleName());
			result.put("Obj", promotion);
			result.put("promotionType", typeId);
			return result;
		} else if (typeId == 26) {//满赠促销(加10)
			FullPromotion fullPromotion = new FullPromotion();
			fullPromotion = (FullPromotion) FaceUtil.transformMap2Bean(param, fullPromotion);
			FullPromotion full = (FullPromotion) promotionDao.save(fullPromotion);

			createPromotion(Integer.valueOf(FaceUtil.toStringAndTrim(full.getId())), typeId,
					Integer.valueOf(FaceUtil.toStringAndTrim(param.get("scopeId"))),
					FaceUtil.toStringAndTrim(param.get("productJson")));
			result.put("Obj", full);
			result.put("promotionType", typeId);
		} else if (typeId == 28) {//满减
			FullCut fullCut = new FullCut();
			fullCut = (FullCut) FaceUtil.transformMap2Bean(param, fullCut);
			FullCut cut = (FullCut) promotionDao.save(fullCut);

			createPromotion(Integer.valueOf(FaceUtil.toStringAndTrim(cut.getId())), typeId,
					Integer.valueOf(FaceUtil.toStringAndTrim(param.get("scopeId"))),
					FaceUtil.toStringAndTrim(param.get("productJson")));
			result.put("Obj", cut);
			result.put("promotionType", typeId);
		} else if (typeId == 30) { //阶梯满减
			SaveFullLadder ladder = new SaveFullLadder();
			ladder = (SaveFullLadder) FaceUtil.transformMap2Bean(param, ladder);
			SaveFullLadder fullLadder = (SaveFullLadder) promotionDao.save(ladder);

			createPromotion(Integer.valueOf(FaceUtil.toStringAndTrim(fullLadder.getId())), typeId,
					Integer.valueOf(FaceUtil.toStringAndTrim(param.get("scopeId"))),
					FaceUtil.toStringAndTrim(param.get("productJson")));
			result.put("Obj", fullLadder);
			result.put("promotionType", typeId);
		} else if (typeId == 1) {// 赠品促销
			PromotionalGifts promotionalGifts = new PromotionalGifts();
			promotionalGifts = (PromotionalGifts) FaceUtil.transformMap2Bean(param, promotionalGifts);
			PromotionalGifts returnData = (PromotionalGifts) promotionDao.save(promotionalGifts);
			createPromotion(Integer.valueOf(FaceUtil.toStringAndTrim(returnData.getId())), typeId,
					Integer.valueOf(FaceUtil.toStringAndTrim(param.get("scopeId"))),
					FaceUtil.toStringAndTrim(param.get("productJson")));
			result.put("Obj", returnData);
			result.put("promotionType", typeId);
		}else if (typeId == 18) {//套装
			SetPromotion setPromotion = new SetPromotion();
			setPromotion = (SetPromotion) FaceUtil.transformMap2Bean(param, setPromotion);
			SetPromotion set = (SetPromotion) promotionDao.save(setPromotion);
			createPromotion(Integer.valueOf(FaceUtil.toStringAndTrim(set.getId())),
					typeId,
					Integer.valueOf(FaceUtil.toStringAndTrim(param.get("scopeId"))),
					FaceUtil.toStringAndTrim(param.get("productJson")));
			result.put("Obj", set);
			result.put("promotionType", typeId);
		}else if(typeId == 0){//满N件减M件
			NmPromotion nmp = new NmPromotion();
			nmp = (NmPromotion)FaceUtil.transformMap2Bean(param, nmp);
			NmPromotion nmpro = (NmPromotion)promotionDao.save(nmp);
			log.info("活动信息："+nmpro.getId()+"  "+nmpro.getSaleName());
			createPromotion(Integer.valueOf(FaceUtil.toStringAndTrim(nmp.getId())),
					typeId,
					Integer.valueOf(FaceUtil.toStringAndTrim(param.get("scopeId"))),
					FaceUtil.toStringAndTrim(param.get("productJson")));
			result.put("Obj", nmpro);
			result.put("promotionType", typeId);
		}
		return result;
	}

	/**
	 * 删除数据以及关系
	 * 
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{id}", method = { RequestMethod.DELETE })
	public String deletePromotion(@PathVariable Integer id) {
		log.info("删除节点id为：" + id + "的数据");
		Object o = promotionDao.deleteRelationships(id);// 删除该数据，并且删除关系
		return "123";
	}

	/**
	 * 修改信息
	 * 
	 * @param p
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "", method = { RequestMethod.PUT })
	public Promotion upPromotion(@RequestBody Promotion p) {
		// promotionDao.updatePromotion(id, map);
		promotionDao.save(p);

		return p;
	}

	/**
	 * 将卖家与活动关联
	 * 
	 * @param p
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/seller", method = { RequestMethod.POST })
	public Seller save(@RequestBody SellerBean seller) {
		Seller s = new Seller();
		s.setSellerName(seller.getSellerName());
		s.setSellerId(seller.getSellerId());
		s.setCreateTime(seller.getCreateTime());

		// s = sellerDao.save(s);

		log.info("添加数据结果,id:" + s.getId() + "  sellerName:" + s.getSellerName());
		sellerDao.createRelationshipJoin(s.getId().intValue(), seller.getPromotionId(), "JOIN");// 关联id和活动id

		return s;
	}

	/**
	 * 根据商品信息，查询对应的活动
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/productPromotion", method = { RequestMethod.GET })
	public List findPromotionByProductId(@RequestParam String productId) {
		List list = new ArrayList<Promotion>();

		list = promotionDao.findProByProductInfo(productId);
		log.info("查询商品对应的活动信息，共："+list.size()+"条");
		return list;
	}

	/**
	 * 根据卖家信息，查询对应的活动
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/typePromotion", method = { RequestMethod.GET })
	public List<Promotion> findPromotionByTypeId(@RequestParam Long typeId) {
		List<Promotion> list = new ArrayList<Promotion>();
		return list;
	}

	/**
	 * 根据地域信息，查询对应的活动
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/zonePromotion", method = { RequestMethod.GET })
	public List<Promotion> findPromotionByZoneId(@RequestParam Long zoneId) {
		List<Promotion> list = new ArrayList<Promotion>();
		return list;
	}

	public void createPromotion(Integer promotionId, Integer promotionTypeId, Integer scopeId, String productJson) {
		FaceUtil faceUtil = new FaceUtil();
		if (promotionId != -1) {
			promotionDao.createRelationship(promotionId, 32, "Data");
			promotionDao.createRelationshipBelong(promotionId, promotionTypeId, "Belong");
			promotionDao.createRelationshipBelong(promotionId, scopeId, "Belong");
		}

		if (!faceUtil.isNullOrEmpty(productJson) && productJson.startsWith("[") && productJson.endsWith("]")) {
			JSONArray json = productJson != null ? JSONArray.fromObject(productJson) : null;
			if (!faceUtil.isNullOrEmpty(json) && json.size() > 0) {
				for (int i = 0; i < json.size(); i++) {
					JSONObject job = json.getJSONObject(i);

					String productId = job.getString("productId");
					String productName = job.getString("productName");
					String productType = job.getString("productType");
					String productSellerId = job.getString("productSellerId");
					String price = job.getString("price");

					Product pdt = new Product();
					pdt.setProductId(productId);
					pdt.setProductName(productName);
					pdt.setProductType(productType);
					pdt.setProductSellerId(productSellerId);
					pdt.setPrice(price);

					Product pro = productDao.save(pdt);// 创建节点
					// 与活动建立关系
					productDao.createRelationshipJoin(pro.getId().intValue(), promotionId, "JOIN");
				}
			}
		}
	}

	@ResponseBody
	@RequestMapping(value="/calculates",method={RequestMethod.GET})
	public CartBean calculates(@RequestParam Map map) throws NumberFormatException, IfException{
		Map<String,Object> insert = new HashMap<String,Object>();
		
		CartBean cb = new CartBean();
		cb.setPromotionTypeId(Long.valueOf(map.get("promotionTypeId").toString()));
		cb.setSinglePrice(Double.valueOf(map.get("singlePrice").toString()));
		cb.setJoin(Boolean.valueOf(map.get("join").toString()));
		cb.setProductId(map.get("productId").toString());
		cb.setAmount(Integer.valueOf(map.get("amount").toString()));
		cb.setPromotionId(map.get("promotionId").toString());
		cb.setProductName(map.get("productName").toString());
		cb.setProductJson(FaceUtil.toStringAndTrim(map.get("productJson")));
		cb.setProductTotolCount(Double.valueOf(map.get("productTotolCount").toString()));
		if(cb.getPromotionTypeId()==12){
			
		}else if(cb.getPromotionTypeId()==26){
			FullPromotion fullpromotion = (FullPromotion)promotionDao.findNode(Long.parseLong(FaceUtil.toStringAndTrim(cb.getPromotionId())));
			if(cb.isJoin()){
				insert.put("cartBean", cb);
				insert.put("fullpromotion", fullpromotion);
				cb = ruleResult(insert);
				System.out.println(cb.getAfterTotal()+"FullPro");
			}else{
				Double values = (double) (cb.getSinglePrice() *cb.getAmount());
				cb.setAfterTotal(values);
			}
		}else if(cb.getPromotionTypeId()==28){
			FullCut fullCut = (FullCut)promotionDao.findNode(Long.parseLong(FaceUtil.toStringAndTrim(cb.getPromotionId())));
			if(cb.isJoin()){
				insert.put("cartBean", cb);
				insert.put("fullCut", fullCut);
				cb = ruleResult(insert);
				System.out.println(cb.getAfterTotal()+"FullCut");
			}else{
				Double values = (double) (cb.getSinglePrice() * cb.getAmount());
				cb.setAfterTotal(values);
			}
			
		}else if(cb.getPromotionTypeId()==18){
			
			SetPromotion setPromotion = (SetPromotion)promotionDao.findNode(Long.parseLong(FaceUtil.toStringAndTrim(cb.getPromotionId())));
			if(cb.isJoin()){
				insert.put("cartBean", cb);
				insert.put("setPromotion", setPromotion);
				cb = ruleResult(insert);
				
				System.out.println(cb.getAfterTotal()+"SetPro");
				
			}else{
				Double values = (double) (cb.getSinglePrice() * cb.getAmount());
				cb.setAfterTotal(values);
			}
			
		}/*else if(cb.getPromotionTypeId()==30){//阶梯满减
			SaveFullLadder saveFullLadder = (SaveFullLadder)promotionDao.findNode(Long.parseLong(FaceUtil.toStringAndTrim(cb.getPromotionId())));
			mapBean.put("SaveFullLadder", saveFullLadder);
			cb = ruleResult(mapBean);
		}else if(cb.getPromotionTypeId()==1){//满赠促销
			PromotionalGifts promotionalGifts = (PromotionalGifts)promotionDao.findNode(Long.parseLong(FaceUtil.toStringAndTrim(cb.getPromotionId())));
			mapBean.put("PromotionalGifts", promotionalGifts);
			cb = ruleResult(mapBean);
		}*/
		return cb;
	}

	
	
	
	/**
	 * 结算
	 * @throws IfException 
	 * @throws NumberFormatException 
	 */
	@ResponseBody
	@RequestMapping(value="/calculate",method={RequestMethod.GET})
	public CartBean calculate(@RequestParam Map map) throws NumberFormatException, IfException{
		Map<String,Object> insert = new HashMap<String,Object>();
		CartBean cb = new CartBean();
//		cartb = (CartBean)FaceUtil.transMap2Bean(map,cartb);
		cb.setPromotionTypeId(Long.valueOf(map.get("promotionTypeId").toString()));
		cb.setSinglePrice(Double.valueOf(map.get("singlePrice").toString()));
		cb.setJoin(Boolean.valueOf(map.get("join").toString()));
		cb.setProductId(map.get("productId").toString());
		cb.setAmount(Integer.valueOf(map.get("amount").toString()));
		cb.setPromotionId(map.get("promotionId").toString());
		cb.setProductName(map.get("productName").toString());
		cb.setProductJson(FaceUtil.toStringAndTrim(map.get("productJson")));
		cb.setProductTotolCount(Double.valueOf(map.get("productTotolCount").toString()));

		
		if(cb.getPromotionTypeId()==12){
			
		}else if(cb.getPromotionTypeId()==26){
			FullPromotion fullpromotion = (FullPromotion)promotionDao.findNode(Long.parseLong(FaceUtil.toStringAndTrim(cb.getPromotionId())));
			if(cb.isJoin()){
				insert.put("cartBean", cb);
				insert.put("fullpromotion", fullpromotion);
				cb = ruleResult(insert);
			}else{
				Double values = (double) (cb.getSinglePrice() *cb.getAmount());
				cb.setAfterTotal(values);
			}
		}else if(cb.getPromotionTypeId()==28){
			FullCut fullCut = (FullCut)promotionDao.findNode(Long.parseLong(FaceUtil.toStringAndTrim(cb.getPromotionId())));
			if(cb.isJoin()){
				insert.put("cartBean", cb);
				insert.put("fullCut", fullCut);
				cb = ruleResult(insert);
			}else{
				Double values = (double) (cb.getSinglePrice() * cb.getAmount());
				cb.setAfterTotal(values);
			}
			
		}else if(cb.getPromotionTypeId()==18){
			
			SetPromotion setPromotion = (SetPromotion)promotionDao.findNode(Long.parseLong(FaceUtil.toStringAndTrim(cb.getPromotionId())));
			if(cb.isJoin()){
				insert.put("cartBean", cb);
				insert.put("setPromotion", setPromotion);
				cb = ruleResult(insert);
			}else{
				Double values = (double) (cb.getSinglePrice() * cb.getAmount());
				cb.setAfterTotal(values);
			}
			
		}else if(cb.getPromotionTypeId()==30){//阶梯满减
			
			insert.put("cartBean", cb);
			SaveFullLadder saveFullLadder = (SaveFullLadder)promotionDao.findNode(Long.parseLong(FaceUtil.toStringAndTrim(cb.getPromotionId())));
			insert.put("SaveFullLadder", saveFullLadder);
			cb = ruleResult(insert);
		}else if(cb.getPromotionTypeId()==1){//满赠促销
			
			insert.put("cartBean", cb);
			PromotionalGifts promotionalGifts = (PromotionalGifts)promotionDao.findNode(Long.parseLong(FaceUtil.toStringAndTrim(cb.getPromotionId())));
			insert.put("PromotionalGifts", promotionalGifts);
			cb = ruleResult(insert);
		}else if(cb.getPromotionTypeId()==0){//满N件减M件
			
			NmPromotion nmp = (NmPromotion)promotionDao.findNode(Long.valueOf(map.get("promotionId").toString()));
			if(cb.isJoin() && cb.getAmount()>=nmp.getFullPiece()){//参加活动且购买数量满足条件
				
				insert.put("nmPromotion", nmp);
				ruleResult(insert);
				log.info("计算促销后的总价："+cb.getAfterTotal());
			}else{
				cb.setAfterTotal(cb.getSinglePrice()*cb.getAmount());
			}
		}
		
		return cb;
	}
	
	/**
	 * 处理规则公共方法
	 * @param list
	 * @return
	 */
	public CartBean ruleResult(Map<String,Object> map){
		
		KieServices ks = KieServices.Factory.get();
		KieContainer kContainer = ks.getKieClasspathContainer();
		KieSession kSession = kContainer.newKieSession("session-promotion");

		Iterator i = map.entrySet().iterator();   
		while(i.hasNext()){
			 Entry  entry=(Entry)i.next();   
			 kSession.insert(entry.getValue());
		}
		kSession.fireAllRules();
		kSession.dispose();
		
		return (CartBean)map.get("cartBean");
	}
	
}
