package com.pet.backend.ad;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "advertisments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Advertisement {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, columnDefinition = "text")
	private String title;

	@Column(name = "image_url", nullable = false, columnDefinition = "text")
	private String imageUrl;

	@Column(name = "link_url", nullable = false, columnDefinition = "text")
	private String linkUrl;

	@Column(columnDefinition = "text")
	private String placement;

	@Column(nullable = false)
	private Integer priority;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@Column(name = "start_date", nullable = false)
	private Instant startDate;

	@Column(name = "end_date", nullable = false)
	private Instant endDate;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

}
