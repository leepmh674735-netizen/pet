package com.pet.backend.walk;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
public class WalkWeatherService {

	private static final Duration CACHE_TTL = Duration.ofMinutes(10);
	private static final long CACHE_MAX_SIZE = 500;
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");

	private final KmaClient kmaClient;
	private final Cache<String, KmaWeatherSnapshot> cache;

	@Autowired
	public WalkWeatherService(KmaClient kmaClient) {
		this(kmaClient, Caffeine.newBuilder().expireAfterWrite(CACHE_TTL).maximumSize(CACHE_MAX_SIZE).build());
	}

	WalkWeatherService(KmaClient kmaClinet, Cache<String, KmaWeatherSnapshot> cache) {
		this.kmaClient = kmaClinet;
		this.cache = cache;
	}

	public WalkWeatherResponse getWeather(double lat, double lng) {
		KmaGridConverter.Grid grid = KmaGridConverter.toGrid(lat, lng);
		String cacheKey = grid.nx() + "|" + grid.ny();

		KmaWeatherSnapshot snapshot = cache.getIfPresent(cacheKey);
		if (snapshot == null) {
			snapshot = kmaClient.fetch(grid.nx(), grid.ny());
			cache.put(cacheKey, snapshot);
		}

		double solar = SolarEstimator.estimate(lat, lng, ZonedDateTime.now(KST), snapshot.sky(), snapshot.pty());
		double asphaltTemp = AsphaltTempCalculator.calculate(snapshot.airTemp(), snapshot.windSpeed(), solar);
		RiskLevel riskLevel = RiskLevel.from(asphaltTemp);

		return new WalkWeatherResponse(snapshot.airTemp(), snapshot.humidity(), snapshot.windSpeed(), round1(solar),
				round1(asphaltTemp), riskLevel, snapshot.baseTime());
	}

	private double round1(double value) {
		return Math.round(value * 10) / 10.0;
	}
}
