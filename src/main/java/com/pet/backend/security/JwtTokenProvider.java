package com.pet.backend.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.pet.backend.member.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private final SecretKey key;
	private final long expirationMs;

	public JwtTokenProvider(JwtProperties properties) {
		byte[] secretBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
		if (secretBytes.length < 64) {
			throw new IllegalStateException("Secret key must be at least 64 bytes, but was " + secretBytes.length + " bytes.");
		}
		this.key = Keys.hmacShaKeyFor(secretBytes);
		this.expirationMs = properties.expirationMs();
	}

	public String createAccessToken(Long memberId, Role role) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(String.valueOf(memberId))
				.claim("role", role.name())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(expirationMs)))
				.signWith(key)
				.compact();
	}

	public long expirationSeconds() {
		return expirationMs / 1000;
	}

	public record TokenPayload(Long memberId, Role role) {
	}

	public TokenPayload parse(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return new TokenPayload(
				Long.valueOf(claims.getSubject()),
				Role.valueOf(claims.get("role", String.class))
		);
	}
}