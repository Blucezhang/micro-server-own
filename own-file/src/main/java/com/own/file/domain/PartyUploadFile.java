package com.own.file.domain;

import com.own.file.base.IDomainBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="Par_UploadFile")
public class PartyUploadFile implements IDomainBase {


	@Transient
	public Object getObjectId() {

		return this.relationId;
		}

	@Id
	@Column(name="relationId", unique=true, nullable=false) 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long relationId;
	
	@Column(name="partyId", unique=true, nullable=false) 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long partyId;

	/*@ManyToOne(targetEntity=Party.class, fetch=FetchType.EAGER) 
	@JoinColumn(name="partyId", nullable=false)
	private Party party;*/

	@ManyToOne(targetEntity=UploadFileInfo.class, fetch=FetchType.EAGER) 
	@JoinColumn(name="uploadId", nullable=false)
	private UploadFileInfo uploadFile;

	public Long getRelationId() {
		return relationId;
	}

	public void setRelationId(Long relationId) {
		this.relationId = relationId;
	}

	/*public Party getParty() {
		return party;
	}

	public void setParty(Party party) {
		this.party = party;
	}*/

	public UploadFileInfo getUploadFile() {
		return uploadFile;
	}

	public void setUploadFile(UploadFileInfo uploadFile) {
		this.uploadFile = uploadFile;
	}

	}
