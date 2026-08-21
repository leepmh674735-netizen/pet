package com.pet.backend.place;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoDocument(
		@JsonProperty("place_name") String placeName,
		@JsonProperty("category_name") String categoryName,
		@JsonProperty("address_name") String addressName,
		@JsonProperty("road_address_name") String roadAddressName,
		String x,
		String y,
		@JsonProperty("place_url") String placeUrl,
		String phone
) {}
