package com.own.product.domain;

import lombok.Data;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@Data
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


}
