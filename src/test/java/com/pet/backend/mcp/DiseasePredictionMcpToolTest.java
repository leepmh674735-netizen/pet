package com.pet.backend.mcp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.pet.backend.prediction.DiseasePrediction;
import com.pet.backend.prediction.DiseasePredictionClient;

@ExtendWith(MockitoExtension.class)
class DiseasePredictionMcpToolTest {

    @Mock
    private DiseasePredictionClient diseasePredictionClient;

    private final WebLinks webLinks = new WebLinks("http://localhost:5173");

    @Test
    void 기존_클라이언트에_위임하고_결과를_자연어로_옮긴다() {
        when(diseasePredictionClient.predict(1L))
                .thenReturn(new DiseasePrediction("슬개골 탈구 의심", "MEDIUM", "보행 패턴 이상 감지"));

        DiseasePredictionMcpTool tool = new DiseasePredictionMcpTool(diseasePredictionClient, webLinks);

        String result = tool.getDiseasePrediction(1L);

        verify(diseasePredictionClient).predict(1L);
        assertThat(result).contains("슬개골 탈구 의심");
        assertThat(result).contains("보통");
        assertThat(result).doesNotContain("MEDIUM");
        assertThat(result).contains("http://localhost:5173/skin/diagnosis");
    }
}