package com.pet.backend.chat.dto;

import com.pet.backend.chat.ChatRole;

import jakarta.validation.constraints.NotNull;

public record ChatRoleChangeRequest(
		
		@NotNull(message = "role은 필수입니다.")
		ChatRole role
) {

}
