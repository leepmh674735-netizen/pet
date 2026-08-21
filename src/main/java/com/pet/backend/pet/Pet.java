package com.pet.backend.pet;

import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pet")
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pet_id")
	private Long id;

	@Column(name = "member_id", nullable = false)
	private Long memberId;

	@Column(name = "pet_name", nullable = false, length = 50)
	private String name;

	@Column(length = 50)
	private String breed;

	@Column(name = "birth_date")
	private LocalDate birthDate;

	@Column(name = "profile_image_url", length = 50)
	private String profileImageUrl;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;

	@Version
	@Column(nullable = false)
	private Long version;

	private Pet(Long memberId, String name, String breed, LocalDate birthDate) {
		this.memberId = memberId;
		this.name = name;
		this.breed = breed;
		this.birthDate = birthDate;
	}

	public static Pet register(Long memberId, String name, String breed, LocalDate birthDate) {
		return new Pet(memberId, name, breed, birthDate);
	}

	public void update(String name, String breed, LocalDate birthDate) {
		this.name = name;
		this.breed = breed;
		this.birthDate = birthDate;
	}

	public void changeProfileImage(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public void delete() {
		this.deletedAt = Instant.now();
	}

}
