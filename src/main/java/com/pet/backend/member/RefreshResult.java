package com.pet.backend.member;

import com.pet.backend.member.dto.TokenResponse;

record RefreshResult(TokenResponse response, String refreshToken) {

}
