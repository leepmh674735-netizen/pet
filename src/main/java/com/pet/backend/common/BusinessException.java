package com.pet.backend.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getDefaultMessage());
		this.errorCode = errorCode;
	}

	public BusinessException(ErrorCode errorCode, String overrideMessage) {
		super(overrideMessage);
		this.errorCode = errorCode;
	}
}
