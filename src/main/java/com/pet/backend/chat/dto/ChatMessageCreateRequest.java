package com.pet.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageCreateRequest(
		
		@NotBlank(message = "메세지는 내용은 필수입니다.")
		@Size(max = 1000, message = "메세지는 1000자 이하여야 합니다.")
		String content
) {

}
