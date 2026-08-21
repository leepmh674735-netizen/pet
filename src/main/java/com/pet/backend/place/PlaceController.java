package com.pet.backend.place;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pet.backend.common.ApiResponse;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@RestController
@Validated
@RequiredArgsConstructor
public class PlaceController {

	private final PlaceService placeService;

	@GetMapping("/api/places")
	public ApiResponse<PlaceListResponse> search(
			@RequestParam @NotNull(message = "lat는 필수입니다.") @DecimalMin(value = "-90.0", message = "lat는 -90 이상이어야 합니다.") @DecimalMax(value = "90.0", message = "lat는 90 이하이어야 합니다.") Double lat,

			@RequestParam @NotNull(message = "lng는 필수입니다.") @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0", message = "lng는 180 이하이어야 합니다.") Double lng,

			@RequestParam(required = false) List<PlaceCategory> categories) {
		List<PlaceCategory> targets = (categories == null || categories == null || categories.isEmpty())
				? List.of(PlaceCategory.values())
				: categories;

		return ApiResponse.ok(new PlaceListResponse(placeService.searchAll(targets, lat, lng)));

	}
}
