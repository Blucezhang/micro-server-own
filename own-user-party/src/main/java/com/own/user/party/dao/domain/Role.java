package com.own.user.party.dao.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node
@Data
public class Role {

	@Id
    @GeneratedValue
	private Long Role;
	@Property(name="note")
	private String Note;
	@Property(name="name")
	private String Name;
	@Property(name="orgid")
	private Long OrgId;
	@Property(name="partmentid")
	private Long PartmentId;
}
