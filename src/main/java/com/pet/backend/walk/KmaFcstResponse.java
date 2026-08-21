package com.pet.backend.walk;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
record KmaFcstResponse(Response response) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Response(Body body) {

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Body(Items items) {

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Items(List<Item> item) {

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record Item(String category, String fcstDate, String fcstTime, String fcstValue) {
	}
}