package com.pet.backend.aisearch;

import java.util.List;

import com.pet.backend.place.Place;

public record AiSearchResponse(String message, List<Place> places) {

}
