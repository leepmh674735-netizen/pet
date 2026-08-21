package com.pet.backend.shorts;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ShortsEventCreateRequest(

	@NotBlank(message = "이벤트 종류 필수입니다.")
	String type,
	
	@Min(value = 0, message = "시청 시간은 0이상이어야 합니다.")
	@Max(value = 21_600_000, message = "시청 시간이 너무 깁니다.")
	Integer watchMs
	
) {
}
