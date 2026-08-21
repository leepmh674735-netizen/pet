package com.pet.backend.walk;

final class AsphaltTempCalculator {

	private AsphaltTempCalculator() {

	}

	static double calculate(double airTemp, double windSpeed, double solar) {
		double convectiveCoeff = WalkWeatherConstants.CONVECTIVE_COEFF_BASE
				+ WalkWeatherConstants.CONVECTIVE_COEFF_WIND * windSpeed;
		double solarGain = WalkWeatherConstants.ASPHALT_DAMPING_K * (1 - WalkWeatherConstants.ASPHALT_ALBEDO) * solar
				/ convectiveCoeff;
		return airTemp + solarGain;
	}
}
