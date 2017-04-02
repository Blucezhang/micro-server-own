package face.promotion;

import face.core.FaceUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

//购物结算时，处理参数bean
public class CartBean {
	
	/**
	 * 1.套装促销类型：productJson为json数组字符串，里面包含商品等信息；此时singlePrice为单个套装价格
	 */
	private String productId;
	private String productName;
	private Double singlePrice;
	private Integer amount;//数量
	private Double total;//原始计算总价
	private Double afterTotal;//计算后的总价
	private String productJson;//商品其他属性，如果是套装促销，此处传套装商品信息，如：[{productId:1,price:12,count:2,order:1},{productId:2,price:12,count:2,order:2}]
	private String promotionId;
	private Long promotionTypeId;//活动类型id 
	private boolean join;//是否参加
	private boolean selectedGifts;//是否选择赠品
	private Double productTotolCount;
	
	public Double getProductTotolCount() {
		return productTotolCount;
	}
	public void setProductTotolCount(Double productTotolCount) {
		this.productTotolCount = productTotolCount;
	}
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public Double getSinglePrice() {
		return singlePrice;
	}
	public void setSinglePrice(Double singlePrice) {
		this.singlePrice = singlePrice;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public Double getTotal() {
		return total;
	}
	public void setTotal(Double total) {
		this.total = total;
	}
	public Double getAfterTotal() {
		return afterTotal;
	}
	public void setAfterTotal(Double afterTotal) {
		this.afterTotal = afterTotal;
	}
	public String getProductJson() {
		return productJson;
	}
	public void setProductJson(String productJson) {
		this.productJson = productJson;
	}
	public String getPromotionId() {
		return promotionId;
	}
	public void setPromotionId(String promotionId) {
		this.promotionId = promotionId;
	}
	public Long getPromotionTypeId() {
		return promotionTypeId;
	}
	public void setPromotionTypeId(Long promotionTypeId) {
		this.promotionTypeId = promotionTypeId;
	}
	public boolean isJoin() {
		return join;
	}
	public void setJoin(boolean join) {
		this.join = join;
	}
	public boolean isSelectedGifts() {
		return selectedGifts;
	}
	public void setSelectedGifts(boolean selectedGifts) {
		this.selectedGifts = selectedGifts;
	}
	
	public Double doSome(CartBean c){
		
		String productJson = c.getProductJson();
		Integer conuters = 0;
		Double signle = 0d;
		if (!FaceUtil.isNullOrEmpty(productJson) && productJson.startsWith("[") && productJson.endsWith("]")) {
			JSONArray json = productJson != null ? JSONArray.fromObject(productJson) : null;
			if (!FaceUtil.isNullOrEmpty(json) && json.size() > 0) {
				for (int i = 0; i < json.size(); i++) {
					JSONObject job = json.getJSONObject(i);
	
					String count = job.getString("count");
					String price = job.getString("price");
					conuters = Integer.parseInt(count);
					signle += conuters*Double.parseDouble(price);
				}
			}
			
		}
		return signle;
	}
	
	
	
}
