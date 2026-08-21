package com.pet.backend.walk;

import org.springframework.http.HttpStatus;

import com.pet.backend.common.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WalkErrorCode implements ErrorCode {

	WEATHER_FETCH_FAILED(HttpStatus.BAD_GATEWAY, "WALK_WEATHER_FETCH_FAILED", "날씨 정보를 가져오지 못했습니다. 잠시 후 다시 시도해 주세요."),
	;
	
	private final HttpStatus status;
	private final String code;
	private final String defaultMessage;
}
