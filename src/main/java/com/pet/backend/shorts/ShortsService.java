package com.pet.backend.shorts;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.pet.backend.common.BusinessException;
import com.pet.backend.common.CommonErrorCode;
import com.pet.backend.member.Member;
import com.pet.backend.member.MemberErrorCode;
import com.pet.backend.member.MemberRepository;
import com.pet.backend.pet.Pet;
import com.pet.backend.pet.PetErrorCode;
import com.pet.backend.pet.PetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShortsService {

	private static final int DEFAULT_LIMIT = 10;
	private static final int MAX_LIMIT = 30;

	private static final long NO_EXCLUSION = -1L;
	private static final int MAX_EXCLUDE_IDS = 300;

	private static final String VIDEO_MIME = "video/mp4";
	private static final long MAX_VIDEO_BYTES = 50L * 1024 * 1024;
	private static final byte[] MP4_FTYP = { 'f', 't', 'y', 'p' };

	private static final SecureRandom RANDOM = new SecureRandom();

	private final ShortsRepository shortsRepository;
	private final ShortsLikeRepository shortsLikeRepository;
	private final MemberRepository memberRepository;
	private final PetRepository petRepository;
	private final ShortsStorageClient storageClient;
	private final ShortsEventService eventService;

	@Transactional
	public LikeToggleResponse toggleLike(Long memberId, Long shortId) {
		if (!shortsRepository.existsByIdAndDeletedAtIsNull(shortId)) {
			throw new BusinessException(ShortsErrorCode.NOT_FOUND);
		}

		boolean liked;
		if (shortsLikeRepository.deleteByShortIdAndMemberId(shortId, memberId) > 0) {
			shortsRepository.decreaseLikeCount(shortId);
			liked = false;
		} else {
			shortsLikeRepository.save(ShortsLike.of(shortId, memberId));
			shortsRepository.increaseLikeCount(shortId);
			liked = true;

			eventService.recordInteraction(memberId, shortId, ShortsEventType.LIKE);
		}

		return new LikeToggleResponse(liked, shortsRepository.findLikeCount(shortId));
	}

	public ShortsVideoResponse uploadVideo(Long memberId, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "올릴 영상 파일이 없습니다.");
		}
		if (file.getSize() > MAX_VIDEO_BYTES) {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR,
					"영상은 %dMB 이하만 올릴 수 있습니다.".formatted(MAX_VIDEO_BYTES / 1024 / 1024));
		}
		if (!VIDEO_MIME.equals(file.getContentType())) {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "mp4 영상만 올릴 수 있습니다.");
		}

		byte[] bytes;
		try {
			bytes = file.getBytes();
		} catch (IOException e) {
			throw new BusinessException(ShortsErrorCode.UPLOAD_FAILED, "영상 파일을 읽을 수 없습니다.");
		}

		if (!isMp4(bytes)) {
			throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "mp4 영상이 아닙니다. 확장자만 바꾼 파일은 올릴 수 없습니다.");
		}

		String path = "%d/%d-%s.mp4".formatted(memberId, System.currentTimeMillis(), randomSuffix());
		return new ShortsVideoResponse(storageClient.upload(path, bytes, VIDEO_MIME));
	}

	@Transactional
	public ShortsResponse upload(Long memberId, ShortsCreateRequest request) {

		Member member = memberRepository.findById(memberId).filter(found -> !found.isDeleted())
				.orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));

		List<Pet> pets = findMyPets(memberId, request.petIds());

		Shorts shorts = Shorts.upload(
				memberId,
				request.videoUrl().trim(),
				blankToNull(request.thumbnailUrl()),
				blankToNull(request.caption()),
				toTags(request.topics(), pets),
				request.durationSec());
		
		shortsRepository.save(shorts);

		return ShortsResponse.of(shorts, member.getName());
	}

	private List<Pet> findMyPets(Long memberId, List<Long> petIds) {
		if (petIds == null || petIds.isEmpty()) {
			return List.of();
		}
		return petIds.stream().filter(Objects::nonNull).distinct()
				.map(petId -> petRepository.findByIdAndMemberIdAndDeletedAtIsNull(petId, memberId)
						.orElseThrow(() -> new BusinessException(PetErrorCode.NOT_FOUND)))
				.toList();
	}

	@Transactional(readOnly = true)
	public ShortsFeedResponse getFeed(Long viewerId, List<Long> excludeIds, Integer limit) {
		int size = normalizeLimit(limit);

		List<Long> rankedIds = rankIds(viewerId, toExclusionList(excludeIds), size + 1);
		boolean hasNext = rankedIds.size() > size;
		List<Long> pageIds = hasNext ? rankedIds.subList(0, size) : rankedIds;
		if (pageIds.isEmpty()) {
			return new ShortsFeedResponse(List.of(), false);
		}

		Map<Long, ShortsResponse> byId = shortsRepository.findAllByIds(pageIds).stream()
				.collect(Collectors.toMap(ShortsResponse::id, Function.identity()));
		List<ShortsResponse> items = pageIds.stream().map(byId::get).filter(Objects::nonNull).toList();

		return new ShortsFeedResponse(fillLikedByMe(items, viewerId), hasNext);
	}

	private List<Long> rankIds(Long viewerId, Collection<Long> excludeIds, int limit) {
		if (viewerId == null) {
			return shortsRepository.findRankedIds(excludeIds, limit);
		}
		return shortsRepository.findPersonalizedRankedIds(viewerId, excludeIds, limit);
	}

	private Collection<Long> toExclusionList(List<Long> excludeIds) {
		if (excludeIds == null || excludeIds.isEmpty()) {
			return List.of(NO_EXCLUSION);
		}
		List<Long> cleaned = excludeIds.stream().filter(Objects::nonNull).distinct().toList();
		if (cleaned.size() > MAX_EXCLUDE_IDS) {
			cleaned = cleaned.subList(cleaned.size() - MAX_EXCLUDE_IDS, cleaned.size());
		}
		return Stream.concat(cleaned.stream(), Stream.of(NO_EXCLUSION)).toList();
	}

	private List<ShortsResponse> fillLikedByMe(List<ShortsResponse> items, Long viewerId) {
		if (viewerId == null || items.isEmpty()) {
			return items;
		}
		Set<Long> likedIds = Set.copyOf(
				shortsLikeRepository.findLikedShortIds(
						viewerId, items.stream().map(ShortsResponse::id).toList()));
		return items.stream()
				.map(item -> item.withLikedByMe(likedIds.contains(item.id())))
				.toList();
	}

	private int normalizeLimit(Integer limit) {
		if (limit == null || limit < 1) {
			return DEFAULT_LIMIT;
		}
		return Math.min(limit, MAX_LIMIT);
	}

	private String blankToNull(String value) {
		return (value == null || value.isBlank()) ? null : value.trim();
	}

	private List<String> toTags(List<String> topics, List<Pet> pets) {
		List<String> labels = toTopicLabels(topics);
		List<String> breeds = pets.stream().map(pet -> blankToNull(pet.getBreed())).filter(Objects::nonNull).toList();

		List<String> merged = Stream.concat(labels.stream(), breeds.stream()).distinct().toList();
		return merged.isEmpty() ? null : merged;
	}

	private List<String> toTopicLabels(List<String> topics) {
		if (topics == null) {
			return List.of();
		}
		return topics.stream().filter(Objects::nonNull).map(String::trim)
				.filter(topic -> !topic.isEmpty())
				.map(topic -> ShortsTopic.from(topic)
						.orElseThrow(() -> new BusinessException(CommonErrorCode.VALIDATION_ERROR,
								"'%s'는 선택할 수 없는 주제입니다. 다음 중에서 골라주세요: %s".formatted(topic,
										String.join(", ", ShortsTopic.labels())))))
				.map(ShortsTopic::label).distinct().toList();
	}

	private boolean isMp4(byte[] bytes) {
		if (bytes.length < 12) {
			return false;
		}
		for (int i = 0; i < MP4_FTYP.length; i++) {
			if (bytes[4 + i] != MP4_FTYP[i]) {
				return false;
			}
		}
		return true;
	}

	private String randomSuffix() {
		byte[] buffer = new byte[4];
		RANDOM.nextBytes(buffer);
		return HexFormat.of().formatHex(buffer);
	}
}