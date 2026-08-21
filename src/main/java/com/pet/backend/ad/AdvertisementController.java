package com.pet.backend.ad;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pet.backend.common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
public class AdvertisementController {

	private final AdvertisementService adService;

	@GetMapping
	public ApiResponse<List<AdResponse>> getAds(@RequestParam(required = false) String placement) {
		return ApiResponse.ok(adService.findActiveAds(placement));
	}
}
