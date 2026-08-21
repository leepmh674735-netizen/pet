package com.pet.backend.chat.dto;

import com.pet.backend.chat.ChatRole;

public record ChatMemberResponse(Long memberId, String name, ChatRole role, String profileImageUrl) {

}
