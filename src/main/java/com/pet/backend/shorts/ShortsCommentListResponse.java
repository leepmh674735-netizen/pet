package com.pet.backend.shorts;

import java.util.List;

public record ShortsCommentListResponse(
		List<ShortsCommentResponse> items,
		int totalCount
) {

	
}
