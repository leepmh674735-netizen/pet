package com.pet.backend.place;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlaceCategory {
	
	HOSPITAL("동물병원", "HP8"),
	CAFE("애견카페", "CE7"),
	HOTEL("애견호텔", "AD5");
	
	private final String defaultKeyword;
	private final String kakaoCategorGroupCode;

}
