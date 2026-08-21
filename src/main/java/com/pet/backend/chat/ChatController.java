package com.pet.backend.chat;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.pet.backend.chat.dto.ChatDelegateRequest;
import com.pet.backend.chat.dto.ChatMessageCreateRequest;
import com.pet.backend.chat.dto.ChatMessageResponse;
import com.pet.backend.chat.dto.ChatPinRequest;
import com.pet.backend.chat.dto.ChatReadRequest;
import com.pet.backend.chat.dto.ChatRoleChangeRequest;
import com.pet.backend.chat.dto.ChatRoomResponse;
import com.pet.backend.chat.dto.ChatRoomSaveRequest;
import com.pet.backend.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	@PostMapping("/api/chat/rooms")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ChatRoomResponse> createRoom(@AuthenticationPrincipal Long memberId,
			@Valid @RequestBody ChatRoomSaveRequest request) {
		return ApiResponse.ok(chatService.createRoom(memberId, request));
	}

	@PutMapping("/api/chat/rooms/{roomId}")
	public ApiResponse<ChatRoomResponse> updateRoom(@AuthenticationPrincipal Long memberId, @PathVariable Long roomId,
			@Valid @RequestBody ChatRoomSaveRequest request) {
		return ApiResponse.ok(chatService.updateRoom(memberId, roomId, request));
	}

	@GetMapping("/api/chat/rooms")
	public ApiResponse<List<ChatRoomResponse>> getRooms(@AuthenticationPrincipal Long memberId,
			@RequestParam(required = false) String keyword, @RequestParam(required = false) String category,
			@RequestParam(required = false) String sort) {
		return ApiResponse.ok(chatService.getRooms(memberId, keyword, category, sort));
	}

	@PostMapping("/api/chat/rooms/{roomId}/read")
	public ApiResponse<Void> markRead(@AuthenticationPrincipal Long memberId, @PathVariable Long roomId,
			@Valid @RequestBody ChatReadRequest request) {
		chatService.markRead(memberId, roomId, request.lastReadMessageId());
		return ApiResponse.ok();
	}

	@PostMapping("/api/chat/rooms/{roomId}/join")
	public ApiResponse<ChatRoomResponse> join(@AuthenticationPrincipal Long memberId, @PathVariable Long roomId) {
		return ApiResponse.ok(chatService.join(memberId, roomId));
	}

	@GetMapping("/api/chat/rooms/{roomId}/message")
	public ApiResponse<List<ChatMessageResponse>> getMessage(@AuthenticationPrincipal Long memberId,
			@PathVariable Long roomId, @RequestParam(required = false) Long afterId,
			@RequestParam(required = false) Long beforeId) {
		return ApiResponse.ok(chatService.getMessages(memberId, roomId, afterId, beforeId));
	}

	@PostMapping("/api/chat/rooms/{roomId}/messages")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<ChatMessageResponse> sendMessage(@AuthenticationPrincipal Long memberId,
			@PathVariable Long roomId, @Valid @RequestBody ChatMessageCreateRequest request) {
		return ApiResponse.ok(chatService.sendMessage(memberId, roomId, request));
	}

	@PutMapping("/api/chat/rooms/{roomId}/pin")
	public ApiResponse<Void> pindMessage(@AuthenticationPrincipal Long memberId, @PathVariable Long roomId,
			@Valid @RequestBody ChatPinRequest request) {
		chatService.pinMessage(memberId, roomId, request.messageId());
		return ApiResponse.ok();
	}

	@DeleteMapping("/api/chat/rooms/{roomId}/pin")
	public ApiResponse<Void> unpinMessage(@AuthenticationPrincipal Long memberId, @PathVariable Long roomId) {
		chatService.unpinMessage(memberId, roomId);
		return ApiResponse.ok();
	}

	@GetMapping("/api/chat/rooms/{roomId}/pin")
	public ApiResponse<ChatMessageResponse> getPinnedMessage(@AuthenticationPrincipal Long memberId,
			@PathVariable Long roomId) {
		return ApiResponse.ok(chatService.getPinnedMessage(memberId, roomId));
	}

	@PostMapping("/api/chat/rooms/{roomId}/leave")
	public ApiResponse<Void> leave(@AuthenticationPrincipal Long memberId, @PathVariable Long roomId) {
		chatService.leave(memberId, roomId);
		return ApiResponse.ok();
	}

	@PostMapping("/api/chat/rooms/{roomId}/members/{memberId}/kick")
	public ApiResponse<Void> kick(@AuthenticationPrincipal Long actorId, @PathVariable Long roomId,
			@PathVariable("memberId") Long targetMemberId) {
		chatService.kick(actorId, roomId, targetMemberId);
		return ApiResponse.ok();
	}

	@PatchMapping("/api/chat/rooms/{roomId}/members/{memberId}/role")
	public ApiResponse<Void> changeRole(@AuthenticationPrincipal Long actorId, @PathVariable Long roomId,
			@PathVariable("memberId") Long targetMemberId, @Valid @RequestBody ChatRoleChangeRequest request) {
		chatService.changeRole(actorId, roomId, targetMemberId, request.role());
		return ApiResponse.ok();
	}

	@PostMapping("/api/chat/rooms/{roomId}/delegate")
	public ApiResponse<Void> delegate(@AuthenticationPrincipal Long actorId, @PathVariable Long roomId,
			@Valid @RequestBody ChatDelegateRequest request) {
		chatService.delegate(actorId, roomId, request.memberId());
		return ApiResponse.ok();
	}

	@DeleteMapping("/api/chat/rooms/{roomId}")
	public ApiResponse<Void> deletedRoom(@AuthenticationPrincipal Long memberId, @PathVariable Long roomId) {
		chatService.deleteRoom(memberId, roomId);
		return ApiResponse.ok();
	}
}