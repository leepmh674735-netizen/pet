package com.pet.backend.walk;

import jakarta.validation.constraints.NotNull;

public record GeoPoint(
		@NotNull(message = "path 원소의 lat는 필수 입니다.")
		Double lat,
		@NotNull(message = "path 원소의 lng는 필수 입니다.") 
		Double lng
		
) {

}
