package face.product;

import java.util.List;


public class ProductBean {

	private Long id;
	private String name;
	private String originalPrice;
	private String promotionPrice;
	private String stocksNum;
	private String salesNum;
	private Long categoryId;
	private Long brandId;
	private Long partyId;
	private String content;
	
	private List<Long> productTypeIdList;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOriginalPrice() {
		return originalPrice;
	}
	public void setOriginalPrice(String originalPrice) {
		this.originalPrice = originalPrice;
	}
	public String getPromotionPrice() {
		return promotionPrice;
	}
	public void setPromotionPrice(String promotionPrice) {
		this.promotionPrice = promotionPrice;
	}
	public String getStocksNum() {
		return stocksNum;
	}
	public void setStocksNum(String stocksNum) {
		this.stocksNum = stocksNum;
	}
	public String getSalesNum() {
		return salesNum;
	}
	public void setSalesNum(String salesNum) {
		this.salesNum = salesNum;
	}
	public Long getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(Long categoryId) {
		this.categoryId = categoryId;
	}
	public Long getBrandId() {
		return brandId;
	}
	public void setBrandId(Long brandId) {
		this.brandId = brandId;
	}
	public List<Long> getProductTypeIdList() {
		return productTypeIdList;
	}
	public void setProductTypeIdList(List<Long> productTypeIdList) {
		this.productTypeIdList = productTypeIdList;
	}
	public Long getPartyId() {
		return partyId;
	}
	public void setPartyId(Long partyId) {
		this.partyId = partyId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
