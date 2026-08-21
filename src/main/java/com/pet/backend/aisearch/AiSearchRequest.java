package com.pet.backend.aisearch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AiSearchRequest(
		
		@NotBlank(message = "메세지는 필수입니다.")
		String message,
		
		@NotNull(message = "petId는 필수입니다.")
		Long petId,
		
		Double lat,
		Double lng
) {
	public AiSearchRequest(String message, Long petId) {
	  this(message, petId, null, null);
	}

}
