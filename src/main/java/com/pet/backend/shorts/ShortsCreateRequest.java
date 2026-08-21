package com.pet.backend.shorts;

import java.util.List;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ShortsCreateRequest(
		
		List<Long> petIds,
		
		@NotBlank(message = "영상 주소는 필수입니다.")
		@URL(message = "영상 주소 형식이 올바르지 않습니다.")
		String videoUrl,
		
		@URL(message = "썸네일 주소 형식이 올바르지 않습니다.")
		String thumbnailUrl,
		
		@Size(max = 500, message = "설명은 500자까지 쓸 수 있습니다.")
		String caption,
		
		@Size(max = 5, message = "주제는 5개까지 선택할 수 있습니다.")
		List<
		   @NotBlank(message = "빈 주제는 넣을 수 없습니다.")
		   @Size(max = 30, message = "주제 이름이 너무 깁니다.")
		   String> topics,
		
		@NotNull(message = "영상 길이는 필수입니다.")
		@Min(value = 5, message = "5초 이상만 영상만 올릴 수 있습니다.")
		@Max(value = 30, message = "30초 이하 영상만 올릴 수 있습니다.")
		Integer durationSec
		
) {

}
