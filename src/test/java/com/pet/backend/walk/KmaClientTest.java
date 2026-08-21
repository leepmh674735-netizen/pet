package com.pet.backend.walk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KmaClientTest {

	@Test
	void 서비스키가_비어있으면_결정적_mock_날씨를_반환한다()  {
	   KmaClient kmaClient = new KmaClient("");
	   
	   KmaWeatherSnapshot snapshot = kmaClient.fetch(60, 127);
	   
	   assertThat(snapshot.airTemp()).isEqualTo(30.0);
	   assertThat(snapshot.windSpeed()).isEqualTo(1.5);
	   assertThat(snapshot.humidity()).isEqualTo(60.0);
	   assertThat(snapshot.pty()).isZero();
	   assertThat(snapshot.sky()).isEqualTo(1);
	   assertThat(snapshot.baseTime()).hasSize(12);
	}
	
	
	@Test
	void 서비스키가_null이어도_mock_날씨를_반환한다() {
	   KmaClient kmaClient = new KmaClient(null);
	   
	   KmaWeatherSnapshot snapshot = kmaClient.fetch(60, 127);
	   
	   assertThat(snapshot.airTemp()).isEqualTo(30.0);
	}
}
