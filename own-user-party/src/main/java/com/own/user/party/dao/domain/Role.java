package com.own.user.party.dao.domain;

import lombok.Data;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity
@Data
public class Role {

	@GraphId
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
