package com.own.product.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;

@Data
@Node
public class Party {

	@Id
    @GeneratedValue
	private Long id;
	private Integer partyTypeId;
	private String partyTypeName;

}
