package com.pet.backend.pet;

import org.springframework.http.HttpStatus;

import com.pet.backend.common.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum PetErrorCode implements ErrorCode {

	NOT_FOUND(HttpStatus.NOT_FOUND, "PET_NOT_FOUND", "반려동물 찾을 수 없습니다."),
	;
	
	private final HttpStatus status;
	private final String code;
	private final String defaultMessage;
}
