package com.pet.backend.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.backend.common.ApiResponse;
import com.pet.backend.common.ErrorCode;
import com.pet.backend.member.MemberErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		ErrorCode code = MemberErrorCode.TOKEN_EXPIRED;
		response.setStatus(code.getStatus().value());
		response.setContentType("/applcation/json;charset=UTF-8");
		response.getWriter()
				.write(objectMapper.writeValueAsString(ApiResponse.fail(code.getCode(), code.getDefaultMessage())));
	}
}
