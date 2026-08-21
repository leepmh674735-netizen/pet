package com.pet.backend.chat;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	List<ChatRoom> findByDeletedAtIsNullOrderByCreatedAtDesc();

	@Query("""
			select r from ChatRoom r
			where r.deletedAt is null
			and (:category is null or r.category = :category)
			and (:keyword = ''
			or lower(r.name) like lower(concat('%', :keyword, '%'))
			   or lower(coalesce(r.description, '')) like lower(concat('%', :keyword, '%')))
			   order by r.createdAt desc
			""")
	List<ChatRoom> searchActive(@Param("keyword") String keyword, @Param("category") ChatCategory category);
}
