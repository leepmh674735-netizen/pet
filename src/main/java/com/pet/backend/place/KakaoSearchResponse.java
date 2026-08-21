package com.pet.backend.place;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoSearchResponse(List<KakaoDocument> documents) {

}
