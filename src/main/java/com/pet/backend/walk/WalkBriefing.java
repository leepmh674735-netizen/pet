package com.pet.backend.walk;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
@Table(name = "walk_briefing")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WalkBriefing {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "checked_at", nullable = false)
	private Instant checkedAt;

	@Column(name = "lat")
	private Double lat;

	@Column(name = "lng")
	private Double lng;

	@Column(name = "air_temp")
	private Double airTemp;

	@Column(name = "wind_speed")
	private Double windSpeed;

	@Column(name = "humidity")
	private Double humidity;

	@Column(name = "solar")
	private Double solar;

	@Column(name = "asphaltTemp")
	private Double asphaltTemp;

	@Enumerated(EnumType.STRING)
	@Column(name = "risk_level")
	private RiskLevel riskLevel;;

	@Column(name = "precipitation")
	private Boolean precipitation;

	@Column(name = "gap_days")
	private Integer gapDays;

	@Column(name = "pet_id")
	private Long petId;

	@Convert(converter = WalkBriefingEventConverter.class)
	@Column(name = "event", nullable = false)
	private WalkBriefingEvent event;

	@Column(name = "notify", nullable = false)
	private boolean notify;

	@Column(name = "reason", nullable = false)
	private String reason;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	private WalkBriefing(Instant checkedAt, Double lat, Double lng, Double airTemp, Double windSpeed, Double humidity,
			Double solar, Double ashaltTemp, RiskLevel riskLevel, Boolean precipitation, Integer gapDays, Long petId,
			WalkBriefingEvent enent, boolean notify, String reason) {
		this.checkedAt = checkedAt;
		this.lat = lat;
		this.lng = lng;
		this.airTemp = airTemp;
		this.windSpeed = windSpeed;
		this.humidity = humidity;
		this.solar = solar;
		this.asphaltTemp = ashaltTemp;
		this.riskLevel = riskLevel;
		this.precipitation = precipitation;
		this.gapDays = gapDays;
		this.petId = petId;
		this.event = event;
		this.notify = notify;
		this.reason = reason;
	}

	static  WalkBriefing skipNoRecord(Instant checkedAt, String reason) {
		return new WalkBriefing(checkedAt, null, null, null, null, null, null, null, null, null, null, null,
				WalkBriefingEvent.SKIP_NO_RECORD, false, reason);
	}

	static WalkBriefing judged(Instant checkedAt, double lat, double lng, double airTemp, double windSpeed,
			double humidity, double solar, double asphaltTemp, RiskLevel riskLevel, boolean precipitation, int gapDays,
			Long petId, WalkBriefingEvent event, boolean notify, String reason) {
		return new WalkBriefing(checkedAt, lat, lng, airTemp, windSpeed, humidity, solar, asphaltTemp, riskLevel,
				precipitation, gapDays, petId, event, notify, reason);
	}

}
