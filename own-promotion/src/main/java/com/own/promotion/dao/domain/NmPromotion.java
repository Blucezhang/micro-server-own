package com.own.promotion.dao.domain;

import lombok.Data;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@Data
@NodeEntity
public class NmPromotion extends BasePromotion {
	
	@Property(name="fullPiece")
	private int fullPiece; //满N件
	@Property(name="minusPiece")
	private int minusPiece;//减M件
}
