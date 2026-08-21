package com.pet.backend.walk;

record KmaWeatherSnapshot(
		double airTemp,
		double humidity, 
		double windSpeed,
		int pty, 
		int sky,
		String baseTime
) {

}
