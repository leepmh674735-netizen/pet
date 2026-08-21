package com.pet.backend.walk;

import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record WalkRecordCreateRequest(
		 
		Long petId,
		
		@NotNull(message = "startedAt은 필수입니다.")
		Instant startedAt,
		
		@NotNull(message = "endedAt은 필수입니다.")
		Instant endedAt,
		
		@NotNull(message = "durationSecods는 필수입니다.")
		@PositiveOrZero(message = "durationSecods는 0 이상이어야 합니다.")
		Integer durationSeconds,
		
		@NotNull(message = "distanceMeters는 필수입니다.")
		@PositiveOrZero(message = "distanceMeters는 0 이상이어야 합니다.")
		Double distanceMeters,
		
		@NotEmpty(message = "path는 비어있지 않아야 합니다.")
		@Valid
		List<GeoPoint> path,
		
		Double airTemp,
		Double asphaltTemp
) {
	
	@AssertTrue(message = "startedAt은 endedAt보다 이전이어야 합니다.")
	public boolean isStartBeforeEnd() {
		return startedAt == null || endedAt == null || startedAt.isBefore(endedAt);
	}

}
