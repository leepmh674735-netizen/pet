package com.pet.backend.walk;

import java.time.Instant;
import java.util.List;

public record WalkRecordResponse(
		Long id,
		Long petId, 
		Instant startedAt,
		Instant endedAt,
		Integer durationSeconds,
		Double distanceMeters,
		List<GeoPoint> path,
		Double airTemp,
		Double ashaltTemp,
		Instant createdAt
) {
	
	public static WalkRecordResponse from(WalkRecord entity) {
		return new WalkRecordResponse(
				entity.getId(),
				entity.getPetId(),
				entity.getStartedAt(),
				entity.getEndedAt(),
				entity.getDurationSeconds(),
				entity.getDistanceMeters(),
				entity.getPath(),
				entity.getAirTemp(),
				entity.getAsphaltTemp(),
				entity.getCreatedAt());
	}
	

}
