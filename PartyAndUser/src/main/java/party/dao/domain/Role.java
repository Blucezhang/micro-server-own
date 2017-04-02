package party.dao.domain;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@NodeEntity
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

	public Long getOrgId() {
		return OrgId;
	}

	public void setOrgId(Long orgId) {
		OrgId = orgId;
	}

	public Long getPartmentId() {
		return PartmentId;
	}

	public void setPartmentId(Long partmentId) {
		PartmentId = partmentId;
	}

	public Long getRole() {
		return Role;
	}

	public void setRole(Long role) {
		Role = role;
	}

	public String getNote() {
		return Note;
	}

	public void setNote(String note) {
		Note = note;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

}
