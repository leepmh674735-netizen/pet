package com.pet.backend.place;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.pet.backend.common.BusinessException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PlaceService {

	private static final Duration CACHE_TTL = Duration.ofMinutes(10);
	private static final long CACHE_MAX_SIZE = 1_000;

	private final KakaoClient kakaoClient;
	private final Cache<String, List<Place>> cache;

	@Autowired
	public PlaceService(KakaoClient kakaoClient) {
		this(kakaoClient, Caffeine.newBuilder()
				.expireAfterWrite(CACHE_TTL)
				.maximumSize(CACHE_MAX_SIZE)
				.build());
	}

	PlaceService(KakaoClient kakaoClient, Cache<String, List<Place>> cache) {
		this.kakaoClient = kakaoClient;
		this.cache = cache;
	}

	public List<Place> searchAll(List<PlaceCategory> categories, double lat, double lng) {
		List<Place> merged = new ArrayList<>();
		int failedCount = 0;

		for (PlaceCategory category : categories) {
			try {
				merged.addAll(search(category, null, lat, lng));
			} catch (BusinessException e) {
				failedCount++;
				log.warn("카테고리 {} 장소 검색 실패 - 나머지 카테고리로 계속 진행합니다.", category, e);
			}
		}

		if (!categories.isEmpty() && failedCount == categories.size()) {
			throw new BusinessException(PlaceErrorCode.SEARCH_FAILED);
		}
		return merged;
	}

	public List<Place> search(PlaceCategory category, String keyword, double lat, double lng) {
		String query = (keyword != null && !keyword.isBlank()) ? keyword.trim() : category.getDefaultKeyword();
		String cacheKey = buildCacheKey(category, query, lat, lng);

		List<Place> cached = cache.getIfPresent(cacheKey);
		if (cached != null) {
			return cached;
		}

		KakaoSearchResponse response = kakaoClient.searchKeyword(query, lat, lng, category.getKakaoCategorGroupCode());
		List<Place> places = toPlaces(response, category);
		cache.put(cacheKey, places);
		return places;
	}

	private List<Place> toPlaces(KakaoSearchResponse response, PlaceCategory category) {
		if (response == null || response.documents() == null) {
			return List.of();
		}
		return response.documents().stream().map(doc -> toPlace(doc, category)).toList();
	}

	private Place toPlace(KakaoDocument doc, PlaceCategory category) {
		return new Place(doc.placeName(), category, parseCoordinate(doc.y()), parseCoordinate(doc.x()),
				doc.roadAddressName() != null && !doc.roadAddressName().isBlank() ? doc.roadAddressName()
						: doc.addressName(),
				doc.placeUrl(), blankToEmpty(doc.phone()), blankToEmpty(doc.categoryName()));
	}

	private double parseCoordinate(String value) {
		return value == null || value.isBlank() ? 0.0 : Double.parseDouble(value);
	}

	private String blankToEmpty(String value) {
		return value == null ? "" : value;
	}

	private String buildCacheKey(PlaceCategory category, String query, double lat, double lng) {
		return String.format(Locale.ROOT, "%s|%s|%.3f|%.3f", category, query, lat, lng);
	}
}
