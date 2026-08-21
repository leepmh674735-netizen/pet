package com.pet.backend.member.dto;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.pet.backend.member.RefreshToken;

public record SessionResponse(
		UUID sessionId,
		String deviceInfo,
		Instant loggedInAt,
		Instant lastUsedAt,
		boolean current
) {
	
	public static SessionResponse of(List<RefreshToken> chain, boolean current) {
		RefreshToken latest = chain.stream()
				.max(Comparator.comparing(RefreshToken::getCreatedAt))
				.orElseThrow();
		return new SessionResponse(
				latest.getSessionId(),
				latest.getDeviceInfo(),
				latest.getSessionStartedAt(),
				latest.getCreatedAt(),
				current);
	}

}
