package com.pet.backend.mcp;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.pet.backend.walk.WalkBriefingService;
import com.pet.backend.walk.WalkBriefingSummary;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WalkBriefingMcpTool {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm").withZone(KST);

	private final WalkBriefingService walkBriefingService;
	private final WebLinks webLinks;

	@Tool
	public String getTodaysWalkBriefing() {
		Optional<WalkBriefingSummary> maybeBrefing = walkBriefingService.getTodaysBriefing();
		if (maybeBrefing.isEmpty()) {
			return "오늘 판정 없음 - 아직 오늘 산책 브리핑이 만들어지지 않았어요(백엔드 스케줄이 아직 실행되지 않았을 수 있어요), " + "산책 페이지에서 직접 확인해보세요: "
					+ webLinks.walkUrl();
		}

		WalkBriefingSummary briefing = maybeBrefing.get();
		if (!briefing.shouldNotify()) {
			return "오늘은 특별히 알려드릴 산책 안내가 없어요. 산책 페이지: " + webLinks.walkUrl();
		}

		String message = switch (briefing.eventCode()) {
		case "hot" -> "오늘 %s 기준 아스팔트 온도가 약 %.1f℃(%s)로 확인돼요. 발바닥 화상 위험이 있으니 산책 시간을 조정해 주세요.".formatted(
				TIME_FORMATTER.format(briefing.checkedAt()), briefing.aspaltTemp(),
				WalkWeatherMcpTool.riskLabel(briefing.riskLevel()));
		case "gap_good" -> "마지막 산책 이후 %d일이 지났고 오늘은 날씨도 산책하기 좋은 조건이에요.".formatted(briefing.gapDays());
		default -> briefing.reason();
		};
		return message + "\n산책 페이지: " + webLinks.walkUrl();
	}

}
