package com.pet.backend.device;

import org.springframework.http.HttpStatus;

import com.pet.backend.common.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DeviceErrorCode implements ErrorCode {

	NOT_FOUND(HttpStatus.NOT_FOUND, "DEVICE_NOT_FOUND", "디바이스를 찾을 수 없습니다."),
	SERIAL_DUPLICATED(HttpStatus.CONFLICT, "DEVICE_SERIAL_DUPLICATED", "이미 등록된 시리얼 번호입니다."),
	ALREADY_MAPPED(HttpStatus.CONFLICT, "DEVICE_ALREADY_MAPPED", "해당 반려동물에 이미 디바이스가 매핑되어 있습니다."),;

	private final HttpStatus status;
	private final String code;
	private final String defaultMessage;
}
