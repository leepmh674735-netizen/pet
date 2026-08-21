package com.pet.backend.member;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pet_member")
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 255)
	private String email;

	@Column(length = 60)
	private String password;

	@Column(nullable = false, length = 50)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Provider provider;

	@Column(name = "provider_id", length = 255)
	private String providerId;

	@Column(name = "profile_image_url", length = 500)
	private String profileImageUrl;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@Column(name = "tokens_valid_from")
	private Instant tokensValidFrom;

	private Member(String email, String password, String name, Role role, Provider provider, String providerId) {
		this.email = email;
		this.password = password;
		this.name = name;
		this.role = role;
		this.provider = provider;
		this.providerId = providerId;
	}

	public static Member createLocalMember(String email, String encodePassword, String name) {
		return new Member(email, encodePassword, name, Role.MEBER, Provider.LOCAL, null);
	}

	public static Member createKakaoMember(String email, String name, String providerId) {
		return new Member(email, null, name, Role.MEBER, Provider.KAKAO, providerId);
	}

	public void changePassword(String encodedPasword) {
		this.password = encodedPasword;
		this.tokensValidFrom = Instant.now();
	}

	public boolean isTokenInvalidated(Instant tokenCreatedAt) {
		return tokensValidFrom != null && tokenCreatedAt.isBefore(tokensValidFrom);
	}

	public void changeName(String name) {
		this.name = name;
	}

	public void changeProfileImage(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public void withdraw() {
		this.deletedAt = Instant.now();
		this.tokensValidFrom = Instant.now();
	}

}
