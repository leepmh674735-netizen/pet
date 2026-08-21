package com.pet.backend.pet;

import java.time.Instant;
import java.time.LocalDate;

public record PetResponse(
		Long id,
		String name, 
		String breed, 
		LocalDate birthDate,
		String profileImageUrl,
		Instant  createdAt
) {
	
	public static PetResponse from(Pet pet) {
		return new PetResponse(pet.getId(), pet.getName(), pet.getBreed(),
				pet.getBirthDate(), pet.getProfileImageUrl(), pet.getCreatedAt());
	}

}
