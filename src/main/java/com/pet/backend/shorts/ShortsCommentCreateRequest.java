package com.pet.backend.shorts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShortsCommentCreateRequest(
		
		@NotBlank(message = "댓글 내용은 필수입니다.")
		@Size(max = 500, message = "댓글은 500자까지 쓸 수 있습니다.")
		String content,
		
		Long parentId
) {

}
