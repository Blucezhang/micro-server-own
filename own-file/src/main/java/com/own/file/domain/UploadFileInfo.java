package com.own.file.domain;

import com.own.file.base.IDomainBase;
import lombok.Data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name="UploadFile_Info")
@Data
public class UploadFileInfo implements IDomainBase {


	@Transient
	public Object getObjectId() {

		return this.uploadId;
		}

	@Id
	@Column(name="uploadId", unique=true, nullable=false) 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long uploadId;

	private Integer type;
	private String uploadName;
	private String note;
	private String saveDir;
	private String saveName;
	private String picName;
	private String picType;
}
