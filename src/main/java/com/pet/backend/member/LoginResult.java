package com.pet.backend.member;

import com.pet.backend.member.dto.LoginResponse;

record LoginResult(LoginResponse response, String refreshToken) {

}
