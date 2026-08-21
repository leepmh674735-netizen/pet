package com.pet.backend.member.dto;

import com.pet.backend.member.Member;
import com.pet.backend.member.Provider;
import com.pet.backend.member.Role;

public record MemberResponse(Long id, String email, String name, Role role, Provider provider,
		String profileImageUrl) {
	
	public static MemberResponse from(Member member) {
		return new MemberResponse(
				member.getId(),  
				member.getEmail(), 
				member.getName(), 
				member.getRole(), 
				member.getProvider(), 
				member.getProfileImageUrl());
	}

}
