package com.pet.backend.member.dto;

public record WithdrawRequest(
		String password,
		String confirmPhrase
) {

}
