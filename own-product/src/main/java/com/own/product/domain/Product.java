package com.own.product.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node
public class Product {

	@Id
    @GeneratedValue
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
	@Property(name="skuCode") private String skuCode;
	@Property(name="specification") private String specification;
	@Property(name="saleStatus") private String saleStatus;


}
