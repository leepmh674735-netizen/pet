package com.pet.backend.ad;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdvertisementService {

	private final AdvertisementRepository adRepository;

	public List<AdResponse> findActiveAds(String placement) {
		Instant now = Instant.now();
		List<Advertisement> ads = (placement == null || placement.isBlank()) ? adRepository.findActive(now)
				: adRepository.findActiveByPlacement(now, placement);
		return ads.stream().map(AdResponse::from).toList();
	}

}
