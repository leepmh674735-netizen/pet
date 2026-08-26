package com.pet.backend.mcp;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.pet.backend.common.BusinessException;
import com.pet.backend.place.Place;
import com.pet.backend.place.PlaceCategory;
import com.pet.backend.place.PlaceService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlaceSearchMcpTool {

	private final PlaceService placeService;
	private final WebLinks webLinks;

	@Tool(description = "키워드와 좌표로 동물병원·애견동반 카페·애견동반 호텔 등 주변 장소를 검색한다.")
	public String searchPlaces(@ToolParam(description = "검색 키워드 (예: '24시 동물병원', '애견동반 카페')") String keyword,
			@ToolParam(description = "위도") double lat, @ToolParam(description = "경도") double lng) {

		List<Place> places;
		try {
			places = placeService.search(guessCategory(keyword), keyword, lat, lng);
		} catch (BusinessException e) {
			return e.getMessage();
		}
		if (places.isEmpty()) {
			return "검색 결과가 없어요. 지도에서 직접 찾아보시겠어요? " + webLinks.mapUrl();
		}

		StringBuilder sb = new StringBuilder();
		for (Place place : places) {
			sb.append("- ").append(place.name()).append(" (").append(place.category().getDefaultKeyword()).append(") -")
					.append(place.address());
			if (!place.phone().isBlank()) {
				sb.append(", ").append(place.phone());
			}
			sb.append('\n');
		}
		sb.append("지도에서 보기: ").append(webLinks.mapUrl());
		return sb.toString();
	}

	private PlaceCategory guessCategory(String keyword) {
		String k = keyword == null ? "" : keyword;
		if (k.contains("카페")) {
			return PlaceCategory.CAFE;
		}
		if (k.contains("호텔")) {
			return PlaceCategory.HOTEL;
		}
		return PlaceCategory.HOSPITAL;
	}

}
