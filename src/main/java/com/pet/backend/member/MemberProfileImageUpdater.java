package com.pet.backend.member;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pet.backend.common.BusinessException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberProfileImageUpdater {

	private final MemberRepository memberRepository;
	
	@Transactional
	Member apply(Long memberId, String profileImageUrl) {
		Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
				.orElseThrow(() -> new BusinessException(MemberErrorCode.NOT_FOUND));
		member.changeProfileImage(profileImageUrl);
		return member;
	}
}
