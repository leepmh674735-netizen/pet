package com.pet.backend.shorts;

import java.util.Arrays;
import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;

public enum ShortsEventType {

	VIEW("view"), WATCH("watch"), SKIP("skip"), LIKE("like"), COMMENT("comment"), SHATE("share");

	private final String dbValue;

	ShortsEventType(String dbValue) {
		this.dbValue = dbValue;
	}

	public String dbValue() {
		return dbValue;
	}

	public static Optional<ShortsEventType> from(String value) {
		if (value == null) {
			return Optional.empty();
		}
		String normalized = value.trim().toLowerCase();
		return Arrays.stream(values()).filter(type -> type.dbValue.equals(normalized)).findFirst();
	}

	@Convert
	public static class DbConverter implements AttributeConverter<ShortsEventType, String> {

		@Override
		public String convertToDatabaseColumn(ShortsEventType type) {
			return (type == null) ? null : type.dbValue;
		}

		@Override
		public ShortsEventType convertToEntityAttribute(String value) {

			return from(value).orElse(null);
		}
	}

}
