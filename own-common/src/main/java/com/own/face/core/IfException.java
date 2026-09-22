package com.own.face.core;

public class IfException extends Exception{
	private static final long serialVersionUID = 1L;
	private final String code;

	public IfException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
