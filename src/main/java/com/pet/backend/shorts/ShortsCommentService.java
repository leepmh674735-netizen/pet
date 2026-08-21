package com.pet.backend.shorts;

import com.pet.backend.common.BusinessException;
import com.pet.backend.common.CommonErrorCode;
import com.pet.backend.member.Member;
import com.pet.backend.member.MemberErrorCode;
import com.pet.backend.member.MemberRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShortsCommentService {

    private final ShortsCommentRepository commentRepository;
    private final ShortsCommentLikeRepository commentLikeRepository;
    private final ShortsRepository shortsRepository;
    private final MemberRepository memberRepository;
    private final ShortsEventService eventService;

    @Transactional(readOnly = true)
    public ShortsCommentListResponse list(Long shortId, Long viewerId) {
        if (!shortsRepository.existsByIdAndDeletedAtIsNull(shortId)) {
            throw new BusinessException(ShortsErrorCode.NOT_FOUND);
        }

        List<ShortsCommentRow> rows = commentRepository.findRowsByShortsId(shortId);
        Set<Long> likedIds = findLikedIds(rows, viewerId);

        Map<Long, List<ShortsCommentResponse>> repliesByParent = new LinkedHashMap<>();
        List<ShortsCommentRow> topLevels = new ArrayList<>();
        for (ShortsCommentRow row : rows) {
            if (row.parentId() == null) {
                topLevels.add(row);
            } else {
                repliesByParent
                        .computeIfAbsent(row.parentId(), key -> new ArrayList<>())
                        .add(ShortsCommentResponse.of(row, likedIds.contains(row.id()), List.of()));
            }
        }

        List<ShortsCommentResponse> items = topLevels.stream()
                .map(row -> ShortsCommentResponse.of(
                        row,
                        likedIds.contains(row.id()),
                        repliesByParent.getOrDefault(row.id(), List.of())
                ))
                .toList();

        return new ShortsCommentListResponse(items, rows.size());
    }

    @Transactional
    public ShortsCommentResponse write(Long memberId, Long shortId, ShortsCommentCreateRequest request) {
        if (!shortsRepository.existsByIdAndDeletedAtIsNull(shortId)) {
            throw new BusinessException(ShortsErrorCode.NOT_FOUND);
        }

        Member member = memberRepository.findById(memberId)
                .filter(found -> !found.isDeleted())
                .orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));

        Long parentId = request.parentId();
        if (parentId != null) {
            ShortsComment parent = commentRepository.findByIdAndDeletedAtIsNull(parentId)
                    .orElseThrow(() -> new BusinessException(ShortsErrorCode.COMMENT_NOT_FOUND));
            if (!parent.getShortId().equals(shortId)) {
                throw new BusinessException(ShortsErrorCode.COMMENT_NOT_FOUND, "다른 영상의 댓글에는 답글을 달 수 없습니다.");
            }
            if (parent.isReply()) {
                throw new BusinessException(CommonErrorCode.VALIDATION_ERROR, "답글에는 다시 답글을 달 수 없습니다.");
            }
        }

        ShortsComment comment = ShortsComment.write(shortId, memberId, parentId, request.content().trim());
        commentRepository.save(comment);

        shortsRepository.increaseCommentCount(shortId);
        eventService.recordInteraction(memberId, shortId, ShortsEventType.COMMENT);

        return new ShortsCommentResponse(
                comment.getId(),
                member.getName(),
                member.getProfileImageUrl(),
                comment.getContent(),
                comment.getLikeCount(),
                false,
                comment.getCreatedAt(),
                List.of()
        );
    }

    @Transactional
    public LikeToggleResponse toggleLike(Long memberId, Long commentId) {
        if (commentRepository.findByIdAndDeletedAtIsNull(commentId).isEmpty()) {
            throw new BusinessException(ShortsErrorCode.COMMENT_NOT_FOUND);
        }

        boolean liked;
        if (commentLikeRepository.deleteByCommentIdAndMemberId(commentId, memberId) > 0) {
            commentRepository.decreaseLikeCount(commentId);
            liked = false;
        } else {
            commentLikeRepository.save(ShortsCommentLike.of(commentId, memberId));
            commentRepository.increaseLikeCount(commentId);
            liked = true;
        }

        return new LikeToggleResponse(liked, commentRepository.findLikeCount(commentId));
    }

    private Set<Long> findLikedIds(List<ShortsCommentRow> rows, Long viewerId) {
        if (viewerId == null || rows.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(commentLikeRepository.findLikedCommentIds(
                viewerId,
                rows.stream().map(ShortsCommentRow::id).toList()
        ));
    }
}