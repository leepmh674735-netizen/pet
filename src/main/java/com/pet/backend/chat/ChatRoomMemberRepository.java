package com.pet.backend.chat;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

	boolean existsByRoomIdAndMemberIdAndLeftAtIsNull(Long roomId, Long memberId);

	Optional<ChatRoomMember> findByRoomIdAndMemberIdAndLeftAtIsNull(Long roomId, Long memberId);

	boolean existsByRoomIdAndMemberIdAndLeftReason(Long roomId, Long memberId, ChatLeftReason leftReason);

	List<ChatRoomMember> findByRoomIdAndLeftAtIsNullOrderByJoinedAtAsc(Long roomId);

	interface RoomParticipantCount {
		Long getRoomId();
		long getParticipantCount();
	}

	@Query("""
			select crm.roomId as roomId, count(crm) as participantCount
			from ChatRoomMember crm
			where crm.leftAt is null and crm.roomId in :roomIds
			group by crm.roomId
			""")
	List<RoomParticipantCount> countActiveByRoomIds(@Param("roomIds") List<Long> roomIds);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update ChatRoomMember crm set crm.lastReadMessageId = :messageId
			where crm.roomId = :roomId and crm.memberId = :memberId and crm.leftAt is null
			and (crm.lastReadMessageId is null or crm.lastReadMessageId < :messageId)
			""")
	int markRead(@Param("roomId") Long roomId, @Param("memberId") Long memberId, @Param("messageId") Long messageId);

	interface RoomUnreadCount {
		Long getRoomId();
		long getUnreadCount();
	}

	@Query("""
			select count(crm) > 0 from ChatRoomMember crm
			where crm.memberId = :memberId and crm.role = com.pet.backend.chat.ChatRole.OWNER
			and crm.leftAt is null
			and exists (select 1 from ChatRoom r where r.id = crm.roomId and r.deletedAt is null)
			""")
	boolean existsActiveOwnedRoom(@Param("memberId") Long memberId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update ChatRoomMember crm set crm.leftAt = :now, crm.leftReason = :reason
			where crm.memberId = :memberId and crm.leftAt is null
			""")
	int leaveAllByMemberId(@Param("memberId") Long memberId, @Param("now") Instant now,
			@Param("reason") ChatLeftReason reason);

	@Query("""
			select crm.roomId as roomId, count(msg.id) as unreadCount
			from ChatRoomMember crm
			left join ChatMessage msg
			   on msg.roomId = crm.roomId
			   and msg.id > coalesce(crm.lastReadMessageId, 0L)
			   and msg.senderId <> crm.memberId
			where crm.memberId = :memberId and crm.leftAt is null
			group by crm.roomId
			""")
	List<RoomUnreadCount> countUnreadByMember(@Param("memberId") Long memberId);
}