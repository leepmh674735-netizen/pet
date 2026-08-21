package com.pet.backend.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NameUpdateRequest(
		
		@NotBlank(message = "이름은 필수입니다.")
		@Size(max = 50, message = "이름은 50자 이하여야 합니다.")
		String name
) {

}
