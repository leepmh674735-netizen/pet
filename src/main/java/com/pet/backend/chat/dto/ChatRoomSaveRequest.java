package com.pet.backend.chat.dto;

import com.pet.backend.chat.ChatCategory;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChatRoomSaveRequest(
		
		@NotBlank(message = "방 이름은 필수입니다.")
		@Size(max = 100, message = "방 이름은 100자 이하여야 합니다.")
		String name,
		
		@NotNull(message = "카테고리는 필수입니다.")
		ChatCategory category,
		
		@Size(max = 200, message = "소개는 200자 이하여야 합니다.")
		String description,
		
		@Min(value = 2, message = "정원은 2명 이상이어야 합니다.")
		@Max(value = 100, message = "정원은 100명 이하여야 합니다.")
		Integer maxMembers
		
		
) {

}
