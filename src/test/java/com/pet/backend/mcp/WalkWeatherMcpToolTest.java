package com.pet.backend.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pet.backend.common.BusinessException;
import com.pet.backend.walk.RiskLevel;
import com.pet.backend.walk.WalkErrorCode;
import com.pet.backend.walk.WalkWeatherResponse;
import com.pet.backend.walk.WalkWeatherService;

@ExtendWith(MockitoExtension.class)
class WalkWeatherMcpToolTest {

	@Mock
	private WalkWeatherService walkWeatherService;

	private final WebLinks webLinks = new WebLinks("http://localhost:5173");

	@Test
	void 위험_단계이면_화상_주의_문구와_함께_한국어_라벨로_응답한다() {
		when(walkWeatherService.getWeather(37.5, 127.0))
				.thenReturn(new WalkWeatherResponse(32.0, 60.0, 1.5, 700.0, 40.3, RiskLevel.DANGER, "202608241200"));
		WalkWeatherMcpTool tool = new WalkWeatherMcpTool(walkWeatherService, webLinks);

		String result = tool.getWalkWeather(37.5, 127.0);

		assertThat(result).contains("위험").contains("화상");
	}

	@Test
	void 날씨_조회_실패시_원본_예외_대신_도메인_안내_메세지를_반환한다() {
		when(walkWeatherService.getWeather(37.5, 127.0))
				.thenThrow(new BusinessException(WalkErrorCode.WEATHER_FETCH_FAILED));
		WalkWeatherMcpTool tool = new WalkWeatherMcpTool(walkWeatherService, webLinks);

		String result = tool.getWalkWeather(37.5, 127.0);

		assertThat(result).isEqualTo(WalkErrorCode.WEATHER_FETCH_FAILED.getDefaultMessage());
	}

}
