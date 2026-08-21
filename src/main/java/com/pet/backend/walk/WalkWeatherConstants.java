package com.pet.backend.walk;

final class WalkWeatherConstants {

	private WalkWeatherConstants() {

	}

	static final double ASPHALT_ALBEDO = 0.10;
	static final double ASPHALT_DAMPING_K = 0.45;
	static final double CONVECTIVE_COEFF_BASE = 5.7;
	static final double CONVECTIVE_COEFF_WIND = 3.8;

	static final double SOLAR_CONSTANT = 1000.0;

	static final double SKY_FACTOR_CLEAR = 1.0;
	static final double SKY_FACTOR_PARTLY_CLOUDY = 0.65;
	static final double SKY_FACTOR_CLOUDY = 0.35;
	static final double SKY_FACTOR_PRECIPITATION = 0.2;

	static final double RISK_CAUTION_THRESHOLD = 25.0;
	static final double RISK_DANGER_THRESHOLD = 35.0;
	static final double RISK_SEVERE_THRESHOLD = 50.0;
}
