package product.dao.domain;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity
public class Product {

	@GraphId
	private Long id;
	
	@Property(name="name")
	private String name;
	
	@Property(name="originalPrice")
	private String originalPrice;

	@Property(name="promotionPrice")
	private String promotionPrice;
	
	@Property(name="stocksNum")
	private String stocksNum;
	
	@Property(name="salesNum")
	private String salesNum;
	
	@Property(name="categoryId")
	private Long categoryId;
	
	@Property(name="brandId")
	private Long brandId;
	
	@Property(name="partyId")
	private Long partyId;

	@Property(name="content")
	private String content;

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

	public Long getPartyId() {
		return partyId;
	}

	public void setPartyId(Long partyId) {
		this.partyId = partyId;
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
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}
