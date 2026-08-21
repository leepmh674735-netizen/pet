package com.pet.backend.chat.dto;

import jakarta.validation.constraints.NotNull;

public record ChatReadRequest(
		
		@NotNull(message = "lastReadMessageId는 필수입니다.")
		Long lastReadMessageId
) {

}
