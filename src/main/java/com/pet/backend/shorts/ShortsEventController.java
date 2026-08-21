package com.pet.backend.shorts;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pet.backend.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ShortsEventController {

	private final ShortsEventService eventService;

	@PostMapping("/api/shorts/{shortId}/events")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<Void> record(@AuthenticationPrincipal Long memberId, @PathVariable Long shortId,
			@Valid @RequestBody ShortsEventCreateRequest request) {
		eventService.record(memberId, shortId, request);
		return ApiResponse.ok();
	}
}
