package com.pet.backend.walk;

import java.time.ZonedDateTime;

final class SolarEstimator {

	private static final double KST_STANDARD_MERIDIAN = 135.0;

	private SolarEstimator() {
	}

	static double estimate(double lat, double lng, ZonedDateTime time, int sky, int pty) {
		double clearSky = clearSkySolar(lat, lng, time);
		return clearSky * skyFactor(sky, pty);
	}

	static double clearSkySolar(double lat, double lng, ZonedDateTime time) {
		int dayOfYear = time.getDayOfYear();
		double declinationDeg = 23.44 * Math.sin(Math.toRadians(360.0 / 365.0 * (284 + dayOfYear)));
		double declinationRad = Math.toRadians(declinationDeg);

		double localDecimalHour = time.getHour() + time.getMinute() / 60.0 + time.getSecond() / 3600.0;
		double trueSolarTime = localDecimalHour + (lng - KST_STANDARD_MERIDIAN) / 15.0;

		double hourAngleRad = Math.toRadians(15.0 * (trueSolarTime - 12.0));
		double latRad = Math.toRadians(lat);

		double sinAltitude = Math.sin(latRad) * Math.sin(declinationRad)
				+ Math.cos(latRad) * Math.cos(declinationRad) * Math.cos(hourAngleRad);

		return Math.max(0.0, WalkWeatherConstants.SOLAR_CONSTANT * sinAltitude);
	}

	static double skyFactor(int sky, int pty) {
		if (pty != 0) {
			return WalkWeatherConstants.SKY_FACTOR_PRECIPITATION;
		}
		return switch (sky) {
			case 3 -> WalkWeatherConstants.SKY_FACTOR_PARTLY_CLOUDY;
			case 4 -> WalkWeatherConstants.SKY_FACTOR_CLOUDY; // CLODY 오타 확인 필요
			default -> WalkWeatherConstants.SKY_FACTOR_CLEAR;
		};
	}
}