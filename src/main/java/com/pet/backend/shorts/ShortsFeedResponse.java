package com.pet.backend.shorts;

import java.util.List;

public record ShortsFeedResponse(
		List<ShortsResponse> items,
		boolean hasNext
) {

}
