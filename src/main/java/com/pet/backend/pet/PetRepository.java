package com.pet.backend.pet;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {

	List<Pet> findByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long memberId);

	Optional<Pet> findByIdAndMemberIdAndDeletedAtIsNull(Long id, Long memberId);
}