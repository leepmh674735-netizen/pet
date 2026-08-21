package com.pet.backend.security;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.backend.common.ApiResponse;
import com.pet.backend.common.ErrorCode;
import com.pet.backend.member.MemberErrorCode;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper;

	private static final Set<String> PERMITTED_URIS = Set.of(
			"/api/members/signup", 
			"/api/members/login",
			"/api/members/login/kakao",
			"/api/members/refresh", 
			"/api/members/logout", 
			"/api/ads");

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return PERMITTED_URIS.contains(request.getRequestURI());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith(BEARER_PREFIX)) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(BEARER_PREFIX.length());
		try {
			JwtTokenProvider.TokenPayload payload = jwtTokenProvider.parse(token);
			var authentication = new UsernamePasswordAuthenticationToken(payload.memberId(), null,
					List.of(new SimpleGrantedAuthority("ROLE_" + payload.role().name())));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (ExpiredJwtException e) {
			writeError(response, MemberErrorCode.TOKEN_EXPIRED);
			return;
		} catch (Exception e) {
			writeError(response, MemberErrorCode.TOKEN_INVALID);
			return;
		}

		filterChain.doFilter(request, response);
	}

	private void writeError(HttpServletResponse response, ErrorCode code) throws IOException {
		response.setStatus(code.getStatus().value());
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter()
				.write(objectMapper.writeValueAsString(ApiResponse.fail(code.getCode(), code.getDefaultMessage())));
	}
}