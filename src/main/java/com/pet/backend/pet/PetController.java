package com.pet.backend.pet;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pet.backend.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PetController {

	private final PetService petService;

	@PostMapping("/api/pets")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<PetResponse> register(@AuthenticationPrincipal Long memberId,
			@Valid @RequestBody PetSaveRequest request) {

		return ApiResponse.ok(petService.register(memberId, request));
	}

	@GetMapping("/api/pets")
	public ApiResponse<List<PetResponse>> getMyPets(@AuthenticationPrincipal Long memberId) {
		return ApiResponse.ok(petService.getMyPets(memberId));
	}

	@GetMapping("/api/pets/{petId}")
	public ApiResponse<PetResponse> getPet(@AuthenticationPrincipal Long memberId, @PathVariable Long petId) {
		return ApiResponse.ok(petService.getPet(memberId, petId));
	}

	@PutMapping("/api/pets/{petId}")
	public ApiResponse<PetResponse> update(@AuthenticationPrincipal Long memberId, @PathVariable Long petId,
			@Valid @RequestBody PetSaveRequest request) {
		return ApiResponse.ok(petService.update(memberId, petId, request));
	}

	@PostMapping("/api/pets/{petId}/imgage")
	public ApiResponse<PetResponse> uploadProfileImage(@AuthenticationPrincipal Long memberId, @PathVariable Long petId,
			@RequestPart("file") MultipartFile file) {
		return ApiResponse.ok(petService.uploadProfileImgage(memberId, petId, file));
	}

	@DeleteMapping("/api/pets/{petId}")
	public ApiResponse<Void> delete(@AuthenticationPrincipal Long memberId, @PathVariable Long petId) {
		petService.delete(memberId, petId);
		return ApiResponse.ok();
	}
}
