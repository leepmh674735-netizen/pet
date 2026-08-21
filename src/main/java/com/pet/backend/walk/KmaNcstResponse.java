package com.pet.backend.walk;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pet.backend.walk.KmaFcstResponse.Item;

@JsonIgnoreProperties(ignoreUnknown = true)
record KmaNcstResponse(Response response) {

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
	record Item(String category, String obsrValue) {
	}

}
