package com.pet.backend.walk;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalkBriefingRepository extends JpaRepository<WalkBriefing, Long>{
	
	Optional<WalkBriefing> findFirstByCheckedAtBetweenOrderByCheckedAtDesc(Instant start, Instant end);

}
