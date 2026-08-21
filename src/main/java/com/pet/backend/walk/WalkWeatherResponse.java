package com.pet.backend.walk;

public record WalkWeatherResponse(
		double airTemp,
		double humidity,
		double windSpeed,
		double solar,
		double asphaltTemp,
		RiskLevel riskLevel,
		String baseTime
) {

}
