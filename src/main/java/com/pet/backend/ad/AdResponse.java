package com.pet.backend.ad;

public record AdResponse(Long id, String title, String imageUrl, String linkUrl) {

	public static AdResponse from(Advertisement ad) {
		return new AdResponse(ad.getId(), ad.getTitle(), ad.getImageUrl(), ad.getLinkUrl());
	}
}
