package com.pet.backend.walk;

import java.time.Instant;

public record WalkBriefingSummary(
		String eventCode,
		boolean shouldNotify,
		String reason,
		RiskLevel riskLevel,
		Double aspaltTemp,
		Integer gapDays,
		Long petId,
		Instant checkedAt
) {
	
	static WalkBriefingSummary from(WalkBriefing briefing) {
		return new WalkBriefingSummary(
				briefing.getEvent().code(),
				briefing.isNotify(),
				briefing.getReason(),
				briefing.getRiskLevel(),
				briefing.getAsphaltTemp(),
				briefing.getGapDays(),
				briefing.getPetId(),
				briefing.getCheckedAt());
	}

}
