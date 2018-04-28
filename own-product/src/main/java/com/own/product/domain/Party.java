package com.own.product.domain;

import lombok.Data;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@Data
@NodeEntity
public class Party {

	@GraphId
	private Long id;
	private Integer partyTypeId;
	private String partyTypeName;

}
