package com.own.file.domain;

import com.own.file.base.IDomainBase;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name="Par_UploadFile")
@Data
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


	}
