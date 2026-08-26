package com.pet.backend.member;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface MemberRepository extends JpaRepository<Member, Long> {

	List<MemberDisplay> findByIdIn(Collection<Long> ids);
	
	@Query("select m from Member m where lower(m.email) = :normalizeEmail and m.deletedAt is null")
	Optional<Member> findActiveByNormalizedEmail(@Param("normalizedEmail") String normalizedEmail);

	@Query("select count(m) > 0 from Member m where lower(m.email) = :normalizedEmail and m.deletedAt is null")
	boolean existsActiveByNormalizedEmail(@Param("normalized") String normalizedEmail);

	Optional<Member> findByProviderAndProviderIdAndDeletedAtIsNull(Provider provider, String providerId);

	Optional<Member> findByIdAndDeletedAtIsNull(Long id);

	boolean existsByIdAndDeletedAtIsNull(Long id);

	@Lock(LockModeType.PESSIMISTIC_READ)
	@Query("select m from Member m where m.id = :id")
	Optional<Member> findByIdForShare(@Param("id") Long id);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select m from Member m where m.id = :id and m.deletedAt is null")
	Optional<Member> findActiveByIdForUpdate(@Param("id") Long id);
}
