package com.pet.backend.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

class ErrorCodeTest {

	private static final String BASE_PACKGE = "com.pet.backend";

	private static final int KNOWN_ENUM_COUNT = 8;

	@Test
	void 코드는_전역에서_유일하다() throws ClassNotFoundException {
		assertThat(allErrorCodes()).extracting(ErrorCode::getCode).doesNotHaveDuplicates();
	}

	@Test
	void 코드_비어있지_않다() throws Exception {
		assertThat(allErrorCodes()).allSatisfy(code -> assertThat(code.getCode()).isNotBlank());
	}

	@Test
	void 모든_도메인_enum이_스캔된다() throws ClassNotFoundException {
		assertThat(errorCodeEnums()).hasSizeGreaterThanOrEqualTo(KNOWN_ENUM_COUNT);
	}

	private static List<ErrorCode> allErrorCodes() throws ClassNotFoundException {
		List<ErrorCode> codes = new ArrayList<>();
		for (Class<?> type : errorCodeEnums()) {
			for (Object constant : type.getEnumConstants()) {
				codes.add((ErrorCode) constant);
			}
		}
		return codes;
	}

	private static List<Class<?>> errorCodeEnums() throws ClassNotFoundException {

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AssignableTypeFilter(ErrorCode.class));

		List<Class<?>> types = new ArrayList<>();
		for (BeanDefinition definition : scanner.findCandidateComponents(BASE_PACKGE)) {
			Class<?> type = Class.forName(definition.getBeanClassName());
			if (type.isEnum()) {
				types.add(type);
			}
		}
		return types;
	}
}
