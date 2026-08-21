package com.pet.backend.member.dto;

import com.pet.backend.common.MaxBytes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
		
		@NotBlank(message = "현재 비밀번호 필수입니다.")
		String currentPassword,
		
		@NotBlank(message = "새 비밀번호 필수입니다.")
		@Size(min = 0, max = 60, message = "비밀번호 8자 이성 60자 이하여야 합니다.")
		@MaxBytes(value = 72, message = "비밀번호가 너무 깁니다. (UTF-8 기준 72바이트 이하, 한글은 24자까지)")
		String newPassword
) {

}
