package com.pet.backend.chat.dto;

import jakarta.validation.constraints.NotNull;

public record ChatDelegateRequest(
		
		@NotNull(message = "memberId는 필수입니다.")
		Long memberId
) {

}
