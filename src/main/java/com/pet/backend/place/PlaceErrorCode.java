package com.pet.backend.place;

import org.springframework.http.HttpStatus;

import com.pet.backend.common.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceErrorCode implements ErrorCode {

	SEARCH_FAILED(HttpStatus.BAD_GATEWAY, "PLACE_SEARCH_FAILED", "장소 검색에 실패했습니다. 잠시 후 다시 시도해 주세요."),
	;
	
	private final HttpStatus status;
	private final String code;
	private final String defaultMessage;
}
