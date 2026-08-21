package com.pet.backend.shorts;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pet.backend.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ShortsController {

	private final ShortsService shortsService;
	private final ShortsCommentService commentService;

	@GetMapping("/api/shorts")
	public ApiResponse<ShortsFeedResponse> getFeed(@AuthenticationPrincipal Long memberId,
			@RequestParam(required = false) List<Long> excludeIds, @RequestParam(required = false) Integer limit) {
		return ApiResponse.ok(shortsService.getFeed(memberId, excludeIds, limit));
	}

	@PostMapping("/api/shorts/{shortId}/like")
	public ApiResponse<LikeToggleResponse> toggleList(@AuthenticationPrincipal Long memberId,
			@PathVariable Long shortsId) {
		return ApiResponse.ok(shortsService.toggleLike(memberId, shortsId));
	}

	@GetMapping(value = "/api/shorts/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<ShortsVideoResponse> uploadVideo(@AuthenticationPrincipal Long memberId,
			@RequestPart("file") MultipartFile file) {
		return ApiResponse.ok(shortsService.uploadVideo(memberId, file));
	}

	@PostMapping("/api/shorts")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ShortsResponse> upload(@AuthenticationPrincipal Long memberId,
			@Valid @RequestBody ShortsCreateRequest request) {
		return ApiResponse.ok(shortsService.upload(memberId, request));
	}

}
