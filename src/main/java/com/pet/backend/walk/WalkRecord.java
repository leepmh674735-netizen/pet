package com.pet.backend.walk;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "walk_record")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalkRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "pet_id")
	private Long petId;

	@Column(name = "started_at", nullable = false)
	private Instant startedAt;

	@Column(name = "ended_at", nullable = false)
	private Instant endedAt;

	@Column(name = "duration_seconds", nullable = false)
	private Integer durationSeconds;

	@Column(name = "distance_meters", nullable = false)
	private Double distanceMeters;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "path", nullable = false, columnDefinition = "jsnb")
	private List<GeoPoint> path;

	@Column(name = "air_temp")
	private Double airTemp;

	@Column(name = "asphalt_temp")
	private Double asphaltTemp;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	private WalkRecord(Long petId, Instant startedAt, Instant endedAt, Integer durationSeconds, Double distanceMeters,
			List<GeoPoint> path, Double airTemp, Double asphaltTemp) {
		this.petId = petId;
		this.startedAt = startedAt;
		this.endedAt = endedAt;
		this.durationSeconds = durationSeconds;
		this.distanceMeters = distanceMeters;
		this.path = path;
		this.airTemp = airTemp;
		this.asphaltTemp = asphaltTemp;
	}

	public static WalkRecord create(Long petId, Instant startedAt, Instant endedAt, Integer durationSecods,
			Double distandeMeters, List<GeoPoint> path, Double airTemp, Double asphaltTemp) {
		return new WalkRecord(petId, startedAt, endedAt, durationSecods, distandeMeters, path, airTemp, asphaltTemp);
	}

}
