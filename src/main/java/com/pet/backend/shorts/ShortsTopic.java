package com.pet.backend.shorts;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum ShortsTopic {

	DAILY("일상/브이로그"),
	OUTDOOR("산책/야외/여행"),
	PLAY("놀이"),
	FOOD("먹방/간식"),

	GROOMING("미용"),
	
	TRAINING("훈련/교육"),
	HEALTH("건강/의료"),
	REVIEW("정보/리뷰"),
	
	CUTE("귀여움"),
	FUNNY("개그/밈"),
	CHALLENGE("챌린지/트렌드"),
	TOUCHING("감동/성장"),
	MUSIC("노래/음악"),
	
	ADOPTION("입양/구조");

	private final String label;

	ShortsTopic(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

	public static Optional<ShortsTopic> from(String value) {
		if (value == null) {
			return Optional.empty();
		}
		String normalized = value.trim();
		return Arrays.stream(values()).filter(topic -> topic.label.equals(normalized)).findFirst();
	}

	public static List<String> labels() {
		return Arrays.stream(values()).map(ShortsTopic::label).toList();
	}

}
