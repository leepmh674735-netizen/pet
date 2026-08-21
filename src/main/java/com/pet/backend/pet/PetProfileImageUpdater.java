package com.pet.backend.pet;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pet.backend.common.BusinessException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PetProfileImageUpdater {

	private final PetRepository petRepository;

	@Transactional
	Pet apply(Long memberId, Long petId, String profileImageUrl) {
		Pet pet = petRepository.findByIdAndMemberIdAndDeletedAtIsNull(petId, memberId)
				.orElseThrow(() -> new BusinessException(PetErrorCode.NOT_FOUND));
		pet.changeProfileImage(profileImageUrl);
		return pet;
	}
}
