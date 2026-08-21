package com.pet.backend.ad;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

	@Query("""
		   select a from Advertisement a
		   where a.isActive = true
		   and a.startDate <= :now
		   and a.endDate >= :now
		   order by a.priority desc, a.id desc
			""")
	List<Advertisement> findActive(@Param("now") Instant now);

	@Query("""
			select a from Advertisement a
			where a.isActive = true
			and a.startDate <= :now
			and a.endDate >= :now
			and (a.placement = :placement or a.placement is null)
			order by a.priority desc, a.id desc
			""")
	List<Advertisement> findActiveByPlacement(@Param("now") Instant now,
			@Param("placement") String placement);
}
