package com.pet.backend.place;

public record Place(
		String name,
		PlaceCategory category,
		double lat,
		double lng,
		String address,
		String placeUrl,
		String phone,
		String categoryDetail
) {

}
