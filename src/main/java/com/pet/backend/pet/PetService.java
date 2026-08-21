package com.pet.backend.pet;

import java.io.IOException;
import java.text.Normalizer;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.pet.backend.ad.AdvertisementController;
import com.pet.backend.common.BusinessException;
import com.pet.backend.common.CommonErrorCode;
import com.pet.backend.common.ImageStorageClient;
import com.pet.backend.member.MemberErrorCode;
import com.pet.backend.member.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PetService {

	private final AdvertisementController advertisementController;

	private final PetRepository petRepository;
	private final ImageStorageClient imageStorageClient;
	private final PetProfileImageUpdater petProfileImageUpdater;
	private final MemberRepository memberRepository;

	@Transactional
	public PetResponse register(Long memberId, PetSaveRequest request) {
		requireActiveMember(memberId);
		Pet pet = Pet.register(memberId, request.name().trim(), normalizeBreed(request.breed()), request.birthDate());
		petRepository.save(pet);
		return PetResponse.from(pet);
	}

	@Transactional(readOnly = true)
	public List<PetResponse> getMyPets(Long memberId) {
		requireActiveMember(memberId);
		return petRepository.findByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(memberId).stream()
				.map(PetResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public PetResponse getPet(Long memberId, Long petId) {
		requireActiveMember(memberId);
		return PetResponse.from(getMyPetOrThrow(memberId, petId));
	}

	@Transactional
	public PetResponse update(Long memberId, Long petId, PetSaveRequest request) {
		requireActiveMember(memberId);
		Pet pet = getMyPetOrThrow(memberId, petId);
		pet.update(request.name().trim(), normalizeBreed(request.breed()), request.birthDate());
		return PetResponse.from(pet);
	}

	@Transactional
	public void delete(Long memberId, Long petId) {
		requireActiveMember(memberId);
		getMyPetOrThrow(memberId, petId).delete();
	}

	public PetResponse uploadProfileImgage(Long memberId, Long petId, MultipartFile file) {
		imageStorageClient.validateImage(file);
		requireActiveMember(memberId);
		getMyPetOrThrow(memberId, petId);

		byte[] bytes;
		try {
			bytes = file.getBytes();
		} catch (IOException e) {
			throw new BusinessException(CommonErrorCode.IMAGE_UPLOAD_FAILED);
		}

		String url = imageStorageClient.upload("pet-" + petId, bytes, file.getContentType());

		Pet pet = petProfileImageUpdater.apply(memberId, petId, url + "?v=" + Instant.now().toEpochMilli());
		return PetResponse.from(pet);
	}

	private void requireActiveMember(Long memberId) {
		if (!memberRepository.existsByIdAndDeletedAtIsNull(memberId)) {
			throw new BusinessException(MemberErrorCode.NOT_FOUND);
		}
	}

	private Pet getMyPetOrThrow(Long memberId, Long petId) {
		return petRepository.findByIdAndMemberIdAndDeletedAtIsNull(petId, memberId)
				.orElseThrow(() -> new BusinessException(PetErrorCode.NOT_FOUND));
	}

	private String normalizeBreed(String breed) {
		return (breed == null || breed.isBlank()) ? null : breed.trim();
	}
}
