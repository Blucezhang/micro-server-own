package com.own.promotion.dao.domain;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node
public class NmPromotion extends BasePromotion {
	
	@Property(name="fullPiece")
	private int fullPiece; //满N件
	@Property(name="minusPiece")
	private int minusPiece;//减M件
}
