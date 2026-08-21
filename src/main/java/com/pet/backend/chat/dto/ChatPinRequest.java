package com.pet.backend.chat.dto;

import jakarta.validation.constraints.NotNull;

public record ChatPinRequest(
		
		@NotNull(message = "messageId는 필수입니다.")
		Long messageId
) {

}
