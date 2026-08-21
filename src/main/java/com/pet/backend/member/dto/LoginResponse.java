package com.pet.backend.member.dto;

import com.pet.backend.member.Member;

public record LoginResponse(
		String accessToken,
		String tokenType,
		long expiresIn,
		MemberResponse user
) {
	
	public static LoginResponse of(String accessToken, long expiresIn, Member member) {
		return new LoginResponse(accessToken, "Bearer", expiresIn, MemberResponse.from(member));
	}

}
