package com.pet.backend.walk;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
class WalkBriefingEventConverter implements AttributeConverter<WalkBriefingEvent, String> {
	
	@Override
	public String convertToDatabaseColumn(WalkBriefingEvent attribute) {
		return attribute == null ? null : attribute.code();
	}
	
	@Override
	public WalkBriefingEvent convertToEntityAttribute(String dbDate) {
		return dbDate == null ? null : WalkBriefingEvent.fromCode(dbDate);
	}

}
