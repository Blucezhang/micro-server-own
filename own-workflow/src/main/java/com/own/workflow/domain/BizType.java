package com.own.workflow.domain;



import lombok.Data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Biz_Type")
@Data
public class BizType implements Serializable {

	private static final long serialVersionUID = 4131056358436993259L;

	@Id
	@Column(name = "bizTypeId", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer bizTypeId;
	private String note;

}
