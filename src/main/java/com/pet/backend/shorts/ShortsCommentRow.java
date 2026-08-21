package com.pet.backend.shorts;

import java.time.Instant;

public record ShortsCommentRow(
		Long id,
		Long parentId,
		String memberName,
		String memberProfileImagUrl,
		String content,
		Integer likeCount,
		Instant createdAt
) {

}
