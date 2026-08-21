package com.pet.backend.shorts;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pet.backend.common.BusinessException;
import com.pet.backend.common.CommonErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortsEventService {

	// [수정] CLTENT_ALLOWED -> CLIENT_ALLOWED 오타 수정
	private static final Set<ShortsEventType> CLIENT_ALLOWED = Set.of(
			ShortsEventType.VIEW,
			ShortsEventType.WATCH,
			ShortsEventType.SKIP
	);

	private final ShortsEventRepository eventRepository;
	private final ShortsRepository shortsRepository;

	@Transactional
	public void record(Long memberId, Long shortId, ShortsEventCreateRequest request) {
		// [수정] 예외 메시지 오타 수정 (없은 -> 없는, Skip -> skip)
		ShortsEventType type = ShortsEventType.from(request.type())
				.filter(CLIENT_ALLOWED::contains)
				.orElseThrow(() -> new BusinessException(
						CommonErrorCode.VALIDATION_ERROR,
						"기록할 수 없는 이벤트 종류입니다. view / watch / skip 중 하나여야 합니다."
				));

		if (!shortsRepository.existsByIdAndDeletedAtIsNull(shortId)) {
			throw new BusinessException(ShortsErrorCode.NOT_FOUND);
		}

		if (type != ShortsEventType.VIEW && request.watchMs() == null) {
			throw new BusinessException(
					CommonErrorCode.VALIDATION_ERROR,
					"%s 이벤트에는 watchMs가 필요합니다.".formatted(type.dbValue())
			);
		}

		Integer watchMs = (type == ShortsEventType.VIEW) ? null : request.watchMs();

		eventRepository.save(ShortsEvent.watching(memberId, shortId, type, watchMs));
	}

	@Transactional
	public void recordInteraction(Long memberId, Long shortId, ShortsEventType type) {
		eventRepository.save(ShortsEvent.interaction(memberId, shortId, type));
	}
}