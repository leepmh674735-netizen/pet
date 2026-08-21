package com.pet.backend.chat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	List<ChatMessage> findTop50ByRoomIdOrderByIdDesc(Long roomId);

	List<ChatMessage> findTop500ByRoomIdAndIdGreaterThanOrderByIdAsc(Long roomId, Long afterId);

	List<ChatMessage> findTop50ByRoomIdAndIdLessThanOrderByIdDesc(Long roomId, Long beforeId);

	Optional<ChatMessage> findTopByRoomIdOrderByIdDesc(Long roomId);

	Optional<ChatMessage> findByIdAndRoomId(Long id, Long roomId);
}
