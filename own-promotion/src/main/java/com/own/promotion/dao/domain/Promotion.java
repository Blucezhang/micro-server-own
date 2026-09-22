package com.own.promotion.dao.domain;

import lombok.Data;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node
public class Promotion extends BasePromotion {
	@Property(name="integral")
	private int integral; //赠送积分
	@Property(name="straightDown")
	private Double straightDown;//直降**元
	@Property(name="disCount")
	private Double disCount;//折扣
	@Property(name="timeLimit")
	private boolean timeLimit;//限时
	@Property(name="amountLimit")
	private boolean amountLimit;//限量
	@Property(name="oneLimit")
	private boolean oneLimit;//限购一个
}
